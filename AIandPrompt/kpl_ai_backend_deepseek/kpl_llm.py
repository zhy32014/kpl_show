# -*- coding: utf-8 -*-
import sys
import io
import os
import hashlib
from dotenv import load_dotenv
from openai import OpenAI, APIError, APIConnectionError, RateLimitError
from cachetools import TTLCache
from typing import Dict

# 修复Windows编码问题
if sys.platform == "win32":
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

# 加载环境变量
load_dotenv()

# 初始化配置
DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY")
MODEL_NAME = os.getenv("MODEL_NAME", "deepseek-chat")
API_TIMEOUT = int(os.getenv("API_TIMEOUT", 60))
TEMPERATURE_DEFAULT = float(os.getenv("TEMPERATURE_DEFAULT", 0.25))

# 初始化缓存
cache = TTLCache(maxsize=100, ttl=3600)

# 初始化DeepSeek客户端（最新官方标准配置）
client = OpenAI(
    api_key=DEEPSEEK_API_KEY,
    base_url="https://api.deepseek.com/v1",  # ✅ 必须加/v1
    timeout=API_TIMEOUT,
    max_retries=2  # ✅ 增加自动重试
)


# 工具函数：生成缓存key
def generate_cache_key(prompt: str, temperature: float) -> str:
    return hashlib.md5(f"{prompt}_{temperature}".encode()).hexdigest()


# 通用大模型调用函数
def call_llm(prompt: str, temperature: float = TEMPERATURE_DEFAULT) -> Dict:
    # 缓存命中直接返回
    cache_key = generate_cache_key(prompt, temperature)
    if cache_key in cache:
        return {"code": 200, "data": cache[cache_key], "msg": "success(cache)"}

    # 参数校验
    if not DEEPSEEK_API_KEY:
        return {"code": 400, "data": "", "msg": "API密钥未配置，请检查.env文件"}
    if not prompt.strip():
        return {"code": 400, "data": "", "msg": "Prompt内容不能为空"}

    try:
        # 调用DeepSeek大模型
        response = client.chat.completions.create(
            model=MODEL_NAME,
            messages=[{"role": "user", "content": prompt}],
            temperature=temperature,
            top_p=0.8,
            max_tokens=1024  # ✅ 测试用，生成快一点
        )
        result = response.choices[0].message.content.strip()
        # 写入缓存
        cache[cache_key] = result
        return {"code": 200, "data": result, "msg": "success"}

    except APIConnectionError:
        return {"code": 500, "data": "", "msg": "网络连接失败，请检查网络后重试"}
    except RateLimitError:
        return {"code": 429, "data": "", "msg": "API调用频率超限，请稍后重试"}
    except APIError as e:
        return {"code": e.status_code or 500, "data": "", "msg": f"大模型调用失败：{e.message}"}
    except Exception as e:
        return {"code": 500, "data": "", "msg": f"系统异常：{str(e)}"}