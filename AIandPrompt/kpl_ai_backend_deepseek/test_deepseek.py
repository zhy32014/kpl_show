import os
from openai import OpenAI

# 直接写死密钥测试（测试完删掉，不要提交）
client = OpenAI(
    api_key="sk-a5c31dc9b9074cb6bf6cf23442e24edb",
    #api_key="sk-4907399c2485489390f06fe0c3f69a08",  # 这里直接粘贴你的密钥，不要用环境变量
    base_url="https://api.deepseek.com/v1"  # 必须加/v1！这是最新要求！
    #base_url="Qwen/Qwen3.5-397B-A17B"
)

try:
    response = client.chat.completions.create(
        model="deepseek-chat",
        messages=[
            {"role": "user", "content": "你好，说一句话"}
        ],
        stream=False,
        timeout=60  # 超时时间改成60秒
    )
    print("✅ 调用成功！")
    print(response.choices[0].message.content)
except Exception as e:
    print("❌ 调用失败")
    print(f"错误类型：{type(e).__name__}")
    print(f"错误信息：{str(e)}")