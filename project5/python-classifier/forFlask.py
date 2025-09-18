from flask import Flask, request, jsonify
import joblib, json, re, os, traceback
from datetime import datetime

app = Flask(__name__)

# ====== 설정 ======
MODEL_FILE = 'hate_speech_classifier.pkl'
ABUSE_DB_FILE = 'final_abuse_db.json'
REQUIRE_API_KEY = os.getenv("REQUIRE_API_KEY", "false").lower() == "true"
API_KEY = os.getenv("API_KEY", "")
THRESHOLD = float(os.getenv("THRESHOLD", "0.5"))  # (모델이 predict_proba 지원 시 사용 예정)

def check_key():
    if REQUIRE_API_KEY:
        k = request.headers.get("X-API-KEY", "")
        if k != API_KEY:
            return jsonify({"error": "unauthorized"}), 401
    return None

print("--- Flask 애플리케이션 시작 ---")

# ====== 1. 욕설 DB 로드 ======
ABUSE_DB = {}
try:
    if os.path.exists(ABUSE_DB_FILE):
        with open(ABUSE_DB_FILE, 'r', encoding='utf-8') as f:
            ABUSE_DB = json.load(f)
        print(f"[OK] 욕설 DB 로드: {ABUSE_DB_FILE}")
    else:
        print(f"[WARN] 욕설 DB 없음: {ABUSE_DB_FILE}")
except Exception as e:
    print("[ERR] 욕설 DB 로드 실패:", e)
    traceback.print_exc()

# 한글 단어경계 이슈를 피하기 위해 \b 제거, 공백/문장부호 기준 느슨한 매칭
abuse_terms = []
if ABUSE_DB:
    for key, data in ABUSE_DB.items():
        abuse_terms.append(re.escape(key))
        if isinstance(data.get('variations'), list):
            for v in data['variations']:
                abuse_terms.append(re.escape(v))

abuse_terms = sorted(set(abuse_terms), key=len, reverse=True)
if abuse_terms:
    # 캡처그룹 사용 (group(0)로 읽어도 됨)
    abuse_regex = re.compile("(" + "|".join(abuse_terms) + ")", re.IGNORECASE)
    print(f"[OK] 욕설 정규식 패턴 {len(abuse_terms)}개 생성")
else:
    abuse_regex = None
    print("[WARN] 욕설 정규식 패턴 없음")

# ====== 2. 모델 로드 ======
MODEL = None
try:
    if os.path.exists(MODEL_FILE):
        MODEL = joblib.load(MODEL_FILE)
        print(f"[OK] 모델 로드: {MODEL_FILE}")
    else:
        print(f"[WARN] 모델 파일 없음: {MODEL_FILE}")
except Exception as e:
    print("[ERR] 모델 로드 실패:", e)
    traceback.print_exc()
    MODEL = None

# ====== 라벨 매핑 ======
LABEL_MAPPING = {
    0: "욕설/비속어",
    1: "성차별/성적 혐오",
    2: "인종/국적 차별",
    3: "지역/정치적 혐오 표현",
    4: "외모 비하",
    5: "특정 세대 혐오",
    6: "종교/사회집단 혐오",
    7: "기타 공격/모욕",
    8: "혐오 표현 아님"
}

DB_TYPE_TO_LABEL_ID = {
    "욕설": [0],
    "비하발언": [4, 5],
    "혐오": [1, 2, 3, 6, 7]
}

def predict_core(comment: str):
    """
    단일 코멘트에 대해 욕설 DB → (필요 시) 모델 순서로 판정.
    반환: dict(comment, is_hate_speech, predicted_labels[list[str]])
    """
    text = (comment or "").strip()
    detected_ids = set()
    is_hate = False

    # 1) 욕설DB 매칭
    if abuse_regex and text:
        m = abuse_regex.search(text)
        if m:
            matched = m.group(0).lower()
            for db_key, db_info in ABUSE_DB.items():
                k = db_key.lower()
                vars_l = [v.lower() for v in db_info.get('variations', [])]
                if matched == k or matched in vars_l:
                    db_type = db_info.get('type')
                    ids = DB_TYPE_TO_LABEL_ID.get(db_type)
                    if isinstance(ids, list):
                        detected_ids.update(ids)
                    elif ids is not None:
                        detected_ids.add(ids)
                    else:
                        detected_ids.add(7)  # 기타 공격/모욕
                    is_hate = True
                    break

    # 2) 모델 예측 (DB에서 못 잡았을 때만)
    if not is_hate and MODEL is not None and text:
        try:
            y = MODEL.predict([text])[0]   # 멀티라벨 바이너리 벡터 가정
            idxs = [i for i, v in enumerate(y) if v == 1]
            if 8 in idxs:
                if len(idxs) == 1:
                    # 오로지 8만 있으면 혐오 아님
                    detected_ids.add(8)
                    is_hate = False
                else:
                    # 8과 다른 혐오 라벨 섞이면 8 제외
                    idxs = [i for i in idxs if i != 8]
                    detected_ids.update(idxs)
                    is_hate = True
            else:
                if idxs:
                    detected_ids.update(idxs)
                    is_hate = True
                else:
                    detected_ids.add(8)
                    is_hate = False
        except Exception as e:
            print("[ERR] 모델 예측 실패:", e)
            traceback.print_exc()
            if not detected_ids:
                detected_ids.add(8)
            is_hate = False

    # 3) 후처리
    final_ids = sorted(list(detected_ids))
    if not final_ids:
        final_ids = [8]
        is_hate = False
    elif 8 in final_ids and len(final_ids) > 1:
        final_ids.remove(8)
        is_hate = True

    final_names = [LABEL_MAPPING.get(i, f"라벨{i}") for i in final_ids]
    return {
        "comment": comment,
        "is_hate_speech": is_hate,
        "predicted_labels": final_names
    }

# ====== 엔드포인트 ======
@app.get("/health")
def health():
    return jsonify({
        "status": "ok",
        "model_loaded": MODEL is not None,
        "abuse_db_loaded": bool(ABUSE_DB),
        "labels": len(LABEL_MAPPING),
        "time": datetime.utcnow().isoformat() + "Z"
    })

@app.post("/predict")   # 자바에서 이걸 호출하는 걸 권장
def predict():
    unauthorized = check_key()
    if unauthorized: return unauthorized
    try:
        data = request.get_json(force=True) or {}
        comment = (data.get("comment") or "").strip()
        if not comment:
            return jsonify({"error": "comment is required"}), 400
        return jsonify(predict_core(comment))
    except Exception as e:
        print("[ERR] /predict 처리 실패:", e)
        traceback.print_exc()
        return jsonify({"error": "internal error", "details": str(e)}), 500

@app.post("/predict_batch")  # 선택: 여러 개 한 번에
def predict_batch():
    unauthorized = check_key()
    if unauthorized: return unauthorized
    try:
        data = request.get_json(force=True) or {}
        comments = data.get("comments") or []
        results = [predict_core(c) for c in comments]
        return jsonify({"results": results})
    except Exception as e:
        print("[ERR] /predict_batch 실패:", e)
        traceback.print_exc()
        return jsonify({"error": "internal error", "details": str(e)}), 500

# 구버전 호환 (네가 기존 /filter_comment를 쓰고 있으면 계속 사용 가능)
@app.post("/filter_comment")
def filter_comment():
    unauthorized = check_key()
    if unauthorized: return unauthorized
    try:
        data = request.get_json(force=True) or {}
        comment = (data.get("comment") or "").strip()
        if not comment:
            return jsonify({"error": "댓글을 입력해주세요."}), 400
        return jsonify(predict_core(comment))
    except Exception as e:
        print("[ERR] /filter_comment 실패:", e)
        traceback.print_exc()
        return jsonify({"error": "internal error", "details": str(e)}), 500

if __name__ == "__main__":
    try:
        # 운영은 gunicorn 권장
        app.run(host="0.0.0.0", port=5000, debug=False)
    except Exception as e:
        print("[FATAL] Flask 실행 실패:", e)
        traceback.print_exc()
