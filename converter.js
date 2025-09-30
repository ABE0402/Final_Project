const fs = require('fs').promises;
const axios = require('axios');

// 💥 중요: 여기에 본인의 카카오 REST API 키를 입력하세요.
const KAKAO_REST_API_KEY = '3936dfb56868dab819976b8713153087';

// 원본 JSON 파일과 새로 저장할 파일 경로
const inputFile = './src/main/resources/static/data/my-places.json';
const outputFile = './src/main/resources/static/data/my-places-with-coords.json';

async function getCoordsFromAddress(address) {
    try {
        const response = await axios.get('https://dapi.kakao.com/v2/local/search/address.json', {
            params: { query: address },
            headers: { Authorization: `KakaoAK ${KAKAO_REST_API_KEY}` }
        });
        if (response.data.documents.length > 0) {
            const doc = response.data.documents[0];
            return { lat: parseFloat(doc.y), lng: parseFloat(doc.x) };
        }
    } catch (error) {
        console.error(`주소 변환 실패: ${address}`, error.message);
    }
    return null;
}

async function processFiles() {
    console.log('원본 JSON 파일을 읽는 중...');
    const data = await fs.readFile(inputFile, 'utf8');
    const locations = JSON.parse(data);

    const locationsWithCoords = [];

    console.log(`총 ${locations.length}개의 주소에 대한 좌표 변환을 시작합니다...`);
    for (const location of locations) {
        const coords = await getCoordsFromAddress(location.address);
        if (coords) {
            locationsWithCoords.push({ ...location, ...coords });
            console.log(`- 성공: ${location.name} (${coords.lat}, ${coords.lng})`);
        } else {
            locationsWithCoords.push({ ...location, lat: null, lng: null });
        }
        await new Promise(resolve => setTimeout(resolve, 50));
    }

    console.log(`좌표 변환 완료. ${outputFile} 파일에 저장 중...`);
    await fs.writeFile(outputFile, JSON.stringify(locationsWithCoords, null, 2));
    console.log('✨ 모든 작업이 완료되었습니다!');
}

processFiles();