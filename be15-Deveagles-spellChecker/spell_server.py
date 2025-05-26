from flask import Flask, request, jsonify
from flask_cors import CORS
from hanspell import spell_checker, constants

app = Flask(__name__)
CORS(app, resources={r"/spellcheck": {"origins": "http://localhost:8080"}})

@app.route('/spellcheck', methods=['POST'])
def spellcheck():
    data = request.get_json()

    work_content = data.get('workContent', '')
    note = data.get('note', '')
    plan = data.get('plan', '')

    def check(text):
        try:
            result = spell_checker.check(text)

            # 🔥 틀린 단어 추출
            error_list = []
            for word, status in result.words.items():
                if status != constants.CheckResult.PASSED:
                    error_list.append({
                        "token": word,
                        "suggestions": [word],  # 실제 추천어는 네이버 API에서 따로 파싱해야 함
                        "type": str(status.name)     # WRONG_SPELLING, WRONG_SPACING 등
                    })

            return {
                "original": result.original,
                "corrected": result.checked,
                "errors": len(error_list),
                "errorList": error_list
            }

        except Exception as e:
            return {
                "original": text,
                "corrected": text,
                "errors": 0,
                "errorList": [],
                "error": f"맞춤법 검사 실패: {str(e)}"
            }

    return jsonify({
        "workContent": check(work_content),
        "note": check(note),
        "plan": check(plan)
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)
