# import os
# from dotenv import load_dotenv
# from fastapi import FastAPI, HTTPException
# from fastapi.middleware.cors import CORSMiddleware
# from pydantic import BaseModel
# import uvicorn
#
# # 导入自定义模块
# from kpl_prompts import KPLPrompts
# from kpl_llm import call_llm
#
# # 加载环境变量
# load_dotenv()
# HOST = os.getenv("HOST", "0.0.0.0")
# PORT = int(os.getenv("PORT", 8000))
#
# # 初始化FastAPI应用
# app = FastAPI(
#     title="KPL赛事智能助手AI接口（DeepSeek版）",
#     description="基于DeepSeek大模型的KPL赛事战术分析、教练建议、解说词生成接口",
#     version="1.0.0"
# )
#
# # 配置CORS（允许前端跨域访问）
# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=["*"],  # 比赛演示用，上线时改成前端域名
#     allow_credentials=True,
#     allow_methods=["*"],
#     allow_headers=["*"],
# )
#
# class ChatRequest(BaseModel):
#     prompt: str                     # 用户输入的自然语言提示词
#     temperature: float = 0.3       # 可选，默认0.3
# # ------------------------------
# # 请求参数模型定义
# # ------------------------------
# class TacticalAnalysisRequest(BaseModel):
#     version: str
#     blue_team: str
#     blue_lineup: str
#     red_team: str
#     red_lineup: str
#     analysis_node: str
#     node_data: str
#
#
# class CoachAdviceRequest(BaseModel):
#     version: str
#     bp_stage: str
#     core_demand: str
#     blue_ban: str = ""
#     blue_pick: str = ""
#     red_ban: str = ""
#     red_pick: str = ""
#
#
# class CommentaryRequest(BaseModel):
#     version: str
#     blue_team: str
#     blue_lineup: str
#     red_team: str
#     red_lineup: str
#     scene_background: str
#     fight_timeline: str
#
#
# # ------------------------------
# # 接口路由定义
# # ------------------------------
# @app.get("/", tags=["健康检查"])
# async def health_check():
#     return {"code": 200, "msg": "KPL AI接口（DeepSeek版）运行正常", "data": ""}
#
#
# @app.post("/ai/chat", tags=["通用自然语言接口"])
# async def chat_api(request: ChatRequest):
#     """接收用户任意自然语言提示词，直接调用大模型"""
#     if not request.prompt or not request.prompt.strip():
#         raise HTTPException(status_code=400, detail="提示词不能为空")
#
#     # 直接调用大模型，temperature 可由用户指定
#     result = call_llm(request.prompt, temperature=request.temperature)
#     if result["code"] != 200:
#         raise HTTPException(status_code=result["code"], detail=result["msg"])
#     return result
#
#
# @app.post("/ai/tactical-analysis", tags=["战术分析接口"])
# async def tactical_analysis_api(request: TacticalAnalysisRequest):
#     """KPL对局战术分析接口，新手科普版"""
#     # 拼接Prompt
#     prompt = KPLPrompts.tactical_analysis()
#     prompt = prompt.replace("{{version}}", request.version)
#     prompt = prompt.replace("{{blue_team}}", request.blue_team)
#     prompt = prompt.replace("{{blue_lineup}}", request.blue_lineup)
#     prompt = prompt.replace("{{red_team}}", request.red_team)
#     prompt = prompt.replace("{{red_lineup}}", request.red_lineup)
#     prompt = prompt.replace("{{analysis_node}}", request.analysis_node)
#     prompt = prompt.replace("{{node_data}}", request.node_data)
#
#     # 调用大模型
#     result = call_llm(prompt, temperature=0.25)
#     if result["code"] != 200:
#         raise HTTPException(status_code=result["code"], detail=result["msg"])
#     return result
#
#
# @app.post("/ai/coach-advice", tags=["教练建议接口"])
# async def coach_advice_api(request: CoachAdviceRequest):
#     """KPL教练级BP建议接口"""
#     # 拼接Prompt
#     prompt = KPLPrompts.coach_advice()
#     prompt = prompt.replace("{{version}}", request.version)
#     prompt = prompt.replace("{{bp_stage}}", request.bp_stage)
#     prompt = prompt.replace("{{blue_ban}}", request.blue_ban)
#     prompt = prompt.replace("{{blue_pick}}", request.blue_pick)
#     prompt = prompt.replace("{{red_ban}}", request.red_ban)
#     prompt = prompt.replace("{{red_pick}}", request.red_pick)
#     prompt = prompt.replace("{{core_demand}}", request.core_demand)
#
#     # 调用大模型
#     result = call_llm(prompt, temperature=0.2)
#     if result["code"] != 200:
#         raise HTTPException(status_code=result["code"], detail=result["msg"])
#     return result
#
#
# @app.post("/ai/commentary", tags=["解说词生成接口"])
# async def commentary_api(request: CommentaryRequest):
#     """KPL官方风格解说词生成接口"""
#     # 拼接Prompt
#     prompt = KPLPrompts.commentary()
#     prompt = prompt.replace("{{version}}", request.version)
#     prompt = prompt.replace("{{blue_team}}", request.blue_team)
#     prompt = prompt.replace("{{blue_lineup}}", request.blue_lineup)
#     prompt = prompt.replace("{{red_team}}", request.red_team)
#     prompt = prompt.replace("{{red_lineup}}", request.red_lineup)
#     prompt = prompt.replace("{{scene_background}}", request.scene_background)
#     prompt = prompt.replace("{{fight_timeline}}", request.fight_timeline)
#
#     # 调用大模型
#     result = call_llm(prompt, temperature=0.35)
#     if result["code"] != 200:
#         raise HTTPException(status_code=result["code"], detail=result["msg"])
#     return result
#
#
# # 启动服务
# if __name__ == "__main__":
#     uvicorn.run(app, host=HOST, port=PORT)

import os
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn

# 导入自定义模块
from kpl_llm import call_llm

# 加载环境变量
load_dotenv()
HOST = os.getenv("HOST", "0.0.0.0")
PORT = int(os.getenv("PORT", 8000))

# 初始化FastAPI应用
app = FastAPI(
    title="KPL赛事智能助手AI接口（自然语言版）",
    description="基于DeepSeek大模型的KPL赛事自然语言问答接口",
    version="2.0.0"
)

# 配置CORS（允许前端跨域访问）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ------------------------------
# 请求参数模型（仅保留自然语言）
# ------------------------------
class ChatRequest(BaseModel):
    prompt: str  # 用户输入的自然语言提示词
    temperature: float = 0.3  # 可选，默认0.3
    session_id: str = "default"  # 可选，用于区分不同会话（后续可扩展多轮记忆）


# ------------------------------
# 系统角色设定（让AI更专业、自然）
# ------------------------------
SYSTEM_PROMPT = (
    "你是一位资深的KPL（王者荣耀职业联赛）分析师。\n"
    "用户会用日常口语向你提问，例如：'分析一下昨天AG超玩会打狼队的团战'、'这赛季哪个英雄最强势？'、'蓝色方先ban大乔，红色方该怎么应对？'\n"
    "你需要理解用户的真实需求，并给出专业、易懂、口语化的回答。\n"
    "如果用户提到具体的比赛、阵容、时间点等信息，请基于你的知识进行分析；\n"
    "如果信息不足，可以礼貌地询问更多细节。\n"
    "回答时请使用自然、亲切的语气，适当加入感叹词和互动（如'嗯～'、'好问题！'、'你看啊'）。"
)


# ------------------------------
# 接口路由定义
# ------------------------------
@app.get("/", tags=["健康检查"])
async def health_check():
    return {"code": 200, "msg": "KPL AI自然语言接口运行正常", "data": ""}


@app.post("/ai/chat", tags=["自然语言问答接口"])
async def chat_api(request: ChatRequest):
    """接收用户任意自然语言提示词，直接调用大模型进行KPL相关分析"""
    if not request.prompt or not request.prompt.strip():
        raise HTTPException(status_code=400, detail="提示词不能为空")

    # 将系统提示和用户问题拼接成一个完整的prompt
    full_prompt = f"{SYSTEM_PROMPT}\n\n用户问题：{request.prompt}\n\n请回答："

    # 调用大模型
    result = call_llm(full_prompt, temperature=request.temperature)
    if result["code"] != 200:
        raise HTTPException(status_code=result["code"], detail=result["msg"])
    return result


# ------------------------------
# 以下为旧版结构化接口（已废弃，保留仅供参考）
# ------------------------------
# from kpl_prompts import KPLPrompts
# class TacticalAnalysisRequest(BaseModel): ...
# @app.post("/ai/tactical-analysis") ...
# @app.post("/ai/coach-advice") ...
# @app.post("/ai/commentary") ...

# 启动服务
if __name__ == "__main__":
    uvicorn.run(app, host=HOST, port=PORT)