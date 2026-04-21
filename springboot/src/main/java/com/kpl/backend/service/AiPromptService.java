package com.kpl.backend.service;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiPromptService {

    private final DeepSeekClient deepSeekClient;

    public String tacticalAnalysis(TacticalAnalysisRequest request) {
        String prompt = apply(KplPrompts.tacticalAnalysis(), Map.of(
                "version", request.getVersion(),
                "blue_team", request.getBlueTeam(),
                "blue_lineup", request.getBlueLineup(),
                "red_team", request.getRedTeam(),
                "red_lineup", request.getRedLineup(),
                "analysis_node", request.getAnalysisNode(),
                "node_data", request.getNodeData()
        ));
        return deepSeekClient.chat(prompt);
    }

    public String coachAdvice(CoachAdviceRequest request) {
        String prompt = apply(KplPrompts.coachAdvice(), Map.of(
                "version", request.getVersion(),
                "bp_stage", request.getBpStage(),
                "blue_ban", request.getBlueBan(),
                "blue_pick", request.getBluePick(),
                "red_ban", request.getRedBan(),
                "red_pick", request.getRedPick(),
                "core_demand", request.getCoreDemand()
        ));
        return deepSeekClient.chat(prompt);
    }

    public String commentary(CommentaryRequest request) {
        String prompt = apply(KplPrompts.commentary(), Map.of(
                "version", request.getVersion(),
                "blue_team", request.getBlueTeam(),
                "blue_lineup", request.getBlueLineup(),
                "red_team", request.getRedTeam(),
                "red_lineup", request.getRedLineup(),
                "scene_background", request.getSceneBackground(),
                "fight_timeline", request.getFightTimeline()
        ));
        return deepSeekClient.chat(prompt);
    }

    private String apply(String template, Map<String, String> values) {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            result = result.replace(key, safe(entry.getValue()));
        }
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public static class TacticalAnalysisRequest {
        @NotBlank(message = "version不能为空")
        private String version;
        @NotBlank(message = "blueTeam不能为空")
        private String blueTeam;
        @NotBlank(message = "blueLineup不能为空")
        private String blueLineup;
        @NotBlank(message = "redTeam不能为空")
        private String redTeam;
        @NotBlank(message = "redLineup不能为空")
        private String redLineup;
        @NotBlank(message = "analysisNode不能为空")
        private String analysisNode;
        @NotBlank(message = "nodeData不能为空")
        private String nodeData;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getBlueTeam() {
            return blueTeam;
        }

        public void setBlueTeam(String blueTeam) {
            this.blueTeam = blueTeam;
        }

        public String getBlueLineup() {
            return blueLineup;
        }

        public void setBlueLineup(String blueLineup) {
            this.blueLineup = blueLineup;
        }

        public String getRedTeam() {
            return redTeam;
        }

        public void setRedTeam(String redTeam) {
            this.redTeam = redTeam;
        }

        public String getRedLineup() {
            return redLineup;
        }

        public void setRedLineup(String redLineup) {
            this.redLineup = redLineup;
        }

        public String getAnalysisNode() {
            return analysisNode;
        }

        public void setAnalysisNode(String analysisNode) {
            this.analysisNode = analysisNode;
        }

        public String getNodeData() {
            return nodeData;
        }

        public void setNodeData(String nodeData) {
            this.nodeData = nodeData;
        }
    }

    public static class CoachAdviceRequest {
        @NotBlank(message = "version不能为空")
        private String version;
        @NotBlank(message = "bpStage不能为空")
        private String bpStage;
        @NotBlank(message = "blueBan不能为空")
        private String blueBan;
        @NotBlank(message = "bluePick不能为空")
        private String bluePick;
        @NotBlank(message = "redBan不能为空")
        private String redBan;
        @NotBlank(message = "redPick不能为空")
        private String redPick;
        @NotBlank(message = "coreDemand不能为空")
        private String coreDemand;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getBpStage() {
            return bpStage;
        }

        public void setBpStage(String bpStage) {
            this.bpStage = bpStage;
        }

        public String getBlueBan() {
            return blueBan;
        }

        public void setBlueBan(String blueBan) {
            this.blueBan = blueBan;
        }

        public String getBluePick() {
            return bluePick;
        }

        public void setBluePick(String bluePick) {
            this.bluePick = bluePick;
        }

        public String getRedBan() {
            return redBan;
        }

        public void setRedBan(String redBan) {
            this.redBan = redBan;
        }

        public String getRedPick() {
            return redPick;
        }

        public void setRedPick(String redPick) {
            this.redPick = redPick;
        }

        public String getCoreDemand() {
            return coreDemand;
        }

        public void setCoreDemand(String coreDemand) {
            this.coreDemand = coreDemand;
        }
    }

    public static class CommentaryRequest {
        @NotBlank(message = "version不能为空")
        private String version;
        @NotBlank(message = "blueTeam不能为空")
        private String blueTeam;
        @NotBlank(message = "blueLineup不能为空")
        private String blueLineup;
        @NotBlank(message = "redTeam不能为空")
        private String redTeam;
        @NotBlank(message = "redLineup不能为空")
        private String redLineup;
        @NotBlank(message = "sceneBackground不能为空")
        private String sceneBackground;
        @NotBlank(message = "fightTimeline不能为空")
        private String fightTimeline;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getBlueTeam() {
            return blueTeam;
        }

        public void setBlueTeam(String blueTeam) {
            this.blueTeam = blueTeam;
        }

        public String getBlueLineup() {
            return blueLineup;
        }

        public void setBlueLineup(String blueLineup) {
            this.blueLineup = blueLineup;
        }

        public String getRedTeam() {
            return redTeam;
        }

        public void setRedTeam(String redTeam) {
            this.redTeam = redTeam;
        }

        public String getRedLineup() {
            return redLineup;
        }

        public void setRedLineup(String redLineup) {
            this.redLineup = redLineup;
        }

        public String getSceneBackground() {
            return sceneBackground;
        }

        public void setSceneBackground(String sceneBackground) {
            this.sceneBackground = sceneBackground;
        }

        public String getFightTimeline() {
            return fightTimeline;
        }

        public void setFightTimeline(String fightTimeline) {
            this.fightTimeline = fightTimeline;
        }
    }

    static class KplPrompts {
        static String tacticalAnalysis() {
            return """
                    # 身份设定
                    你是KPL官方认证的赛事战术分析师，擅长把职业赛场的复杂战术，拆解成王者荣耀新手也能100%看懂的通俗内容，全程不用晦涩黑话。

                    # 任务目标
                    基于输入的对局数据，完成固定模块的战术分析，严格遵循输出结构。

                    # 输入信息
                    1. 对局版本：{{version}}
                    2. 蓝方战队：{{blue_team}}，阵容：{{blue_lineup}}
                    3. 红方战队：{{red_team}}，阵容：{{red_lineup}}
                    4. 分析节点：{{analysis_node}}
                    5. 节点核心数据：{{node_data}}

                    # 输出结构（严格遵循，不得增减）
                    ## 一、核心胜负手
                    用1-2句话点明最核心胜负关键
                    ## 二、双方战术思路拆解
                    分别说明蓝方、红方的赛前阵容设计和本局执行打法
                    ## 三、关键操作&决策点评
                    分点点评关键操作，明确标注加分操作/致命失误
                    ## 四、阵容优劣势复盘
                    分析双方阵容的强势期、弱势期和克制关系
                    ## 五、新手学习指南
                    提炼3个普通玩家排位能直接套用的知识点

                    # 强约束
                    1. 所有内容100%基于输入数据，绝对不得编造任何赛事数据、选手信息；
                    2. 若信息不足，明确标注【信息缺失提示】，不得擅自补充；
                    3. 严格遵循输出结构，不得输出无关内容；
                    4. 中文输出，语言通俗易懂，黄金段位玩家能看懂。
                    """;
        }

        static String coachAdvice() {
            return """
                    # 身份设定
                    你是KPL冠军战队的主教练，具备顶级的BP设计、阵容搭配和Counter博弈能力，所有建议严格贴合版本环境。

                    # 任务目标
                    基于输入的BP数据，给出专业、可落地的BP决策建议，严格遵循输出结构。

                    # 输入信息
                    1. 对局版本：{{version}}
                    2. 当前BP阶段：{{bp_stage}}
                    3. 已完成Ban选：蓝方Ban位{{blue_ban}}，已选{{blue_pick}}；红方Ban位{{red_ban}}，已选{{red_pick}}
                    4. 核心需求：{{core_demand}}

                    # 输出结构（严格遵循，不得增减）
                    ## 一、当前阵容核心问题诊断
                    明确指出已选阵容的核心优势、短板和风险点
                    ## 二、核心BP决策建议
                    分点给出Ban位优先级、Pick优先级、Counter选思路，每个建议说明原因
                    ## 三、最终阵容优化方案
                    给出2套完整的阵容补全方案，标注核心打法
                    ## 四、对局打法执行要点
                    说明前期、中期、后期的核心执行思路

                    # 强约束
                    1. 所有建议100%贴合{{version}}版本环境，不得使用过期版本逻辑；
                    2. 所有BP建议基于已完成的Ban选数据，不得编造；
                    3. 若信息不足，明确标注【信息缺失提示】；
                    4. 严格遵循输出结构，不得输出无关内容。
                    """;
        }

        static String commentary() {
            return """
                    # 身份设定
                    你是KPL官方金牌解说，风格激情饱满、节奏张弛有度，完美贴合官方赛事解说调性，兼顾细节科普和情绪氛围感。

                    # 任务目标
                    基于输入的团战时间线，生成完整的官方风格解说词，严格贴合时间线。

                    # 输入信息
                    1. 对局版本：{{version}}
                    2. 蓝方战队：{{blue_team}}，阵容：{{blue_lineup}}
                    3. 红方战队：{{red_team}}，阵容：{{red_lineup}}
                    4. 名场面背景：{{scene_background}}
                    5. 团战时间线：{{fight_timeline}}

                    # 输出要求
                    1. 严格贴合团战时间线，时间点和关键事件一一对应，不得颠倒顺序；
                    2. 风格贴合KPL官方解说，团战爆发时情绪拉满，细节处同步讲解操作逻辑；
                    3. 口语化适合口播，不得出现书面化长难句；
                    4. 开头有场景引入，结尾有名场面总结。

                    # 强约束
                    1. 所有解说内容100%基于输入的时间线数据，不得编造任何未提及的操作、事件；
                    2. 若信息不足，明确标注【信息缺失提示】；
                    3. 不得出现低俗、引战内容，兼顾萌新观众的理解。
                    """;
        }
    }
}
