from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import kss
import os
import json
import sys

# 1. 모델과 토크나이저 로딩
model_name = "tabularisai/multilingual-sentiment-analysis"
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModelForSequenceClassification.from_pretrained(model_name)
sentiment_score_map = {0: 1, 1: 2, 2: 3, 3: 4, 4: 5}

def predict_sentiment_score(text):
    """단일 문장의 감성을 분석하여 1~5점의 점수를 반환"""
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True, max_length=512)
    with torch.no_grad():
        outputs = model(**inputs)
    probabilities = torch.nn.functional.softmax(outputs.logits, dim=-1)
    sentiment_index = torch.argmax(probabilities, dim=-1).item()
    return sentiment_score_map[sentiment_index]

# 2. JSON 파일에서 키워드 불러오기
try:

    script_dir = os.path.dirname(os.path.abspath(__file__))
    json_path = os.path.join(script_dir, 'keywords.json')

    with open(json_path, 'r', encoding='utf-8') as f:
        aspect_keywords = json.load(f)
except FileNotFoundError:
    print(json.dumps({"error": f"{json_path} 에서 keywords.json을 찾을 수 없습니다."}), file=sys.stderr)
    aspect_keywords = {}

def analyze_aspect_sentiment_korean(review_text):
    """한국어 리뷰의 항목별 감성 점수 평균을 계산하는 함수"""
    sentences = kss.split_sentences(review_text)
    results = {}
    for aspect, keywords in aspect_keywords.items():
        relevant_sentences = [s for s in sentences if any(kw in s for kw in keywords)]
        if relevant_sentences:
            scores = [predict_sentiment_score(s) for s in relevant_sentences]
            average_score = round(sum(scores) / len(scores), 1)
            results[aspect] = average_score

    return results

# 3. Java 호출을 위한 메인 실행 블록
if __name__ == "__main__":
    # Java로부터 커맨드 라인 인자로 리뷰 텍스트를 받음
    if len(sys.argv) > 1:
        review_text = sys.argv[1]

        # 감성 분석 실행
        analysis_result = analyze_aspect_sentiment_korean(review_text)

        # 결과를 JSON 형태의 문자열로 print하여 Java 프로세스에 전달
        print(json.dumps(analysis_result, ensure_ascii=False))