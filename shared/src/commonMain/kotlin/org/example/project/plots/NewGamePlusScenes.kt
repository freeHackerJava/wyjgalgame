package org.example.project.plots

import org.example.project.plotsFormat.Characters.*
import org.example.project.plotsFormat.PlotTree

/**
 * 二周目（New Game+）专属剧情 · “记忆正在崩坏”。
 *
 * 二周目不是“另一个故事”，而是“同一个故事，但记忆正在断裂”。王怡钧带着一周目的
 * 全部记忆重来，却发现记忆像一张被反复折叠的纸——正在错位、混淆、渗漏。
 * 叙事三阶段：
 *  1) 细节的错位：记忆仍清晰，但现实与记忆对不上（年糕、朱皓辰、李佳迪、排名表）。
 *  2) 人物的混淆：她把不同女孩的记忆互相安放，被本人一一戳穿。
 *  3) 现实的渗透：她看见别人看不见的东西——白板上的字、靠墙的“另一个自己”。
 * 结局不提供安慰，只留下一句关于遗忘的提问：「我记不清她们了。」
 *
 * 引擎没有立绘叠影 / 双重画面的渲染能力，所有“虚影 / 错位 / 幻觉”都通过王怡钧的
 * 独白（MONOLOGUE）与旁白（NARRATION）来传达。二周目时间线被大幅压缩（见
 * SaveFormat.checkMilestone / endWeek），把体验重心从经营推向故事本身。
 *
 * 这些变体只覆盖关键场景（序章、NOIP、省队前夜、NOI 前夜、NOI 结局），
 * 其余场景仍复用一周目内容，由 PlayScreenApp.sceneFor 依 SaveFormat.newGamePlus 选择。
 */

// ============================ 序章 · 虚假的信心 ============================

val NgPlusPrologue = PlotTree.plotTree {
	entry("ng-1")
	normal("ng-1", NARRATION, "2024年9月1日，安徽合肥。天气预报说傍晚有雨。", "ng-2")
	normal("ng-2", NARRATION, "王怡钧站在教室门口。她比第一次更平静——因为她“知道”接下来的一切。", "ng-3")
	normal("ng-3", MONOLOGUE, "（我记得这一天。我记得每一周、每一个女孩、每一张排名表。我记得她们会哭、会笑、会问“老师，我是不是不够好”。）", "ng-4")
	normal("ng-4", MONOLOGUE, "（我记得答案。这一次，我知道该怎么回答。）", "ng-5")
	normal("ng-5", NARRATION, "她推开门。四个女孩转过头来。有那么一瞬间，你觉得她们的轮廓边缘像是重叠着另一层看不清的影像——只有你察觉到了。", "ng-6")
	normal("ng-6", LZN, "老师你好！我叫刘子诺！我算法学得还行，就是……偶尔会发挥失常，一点点。", "ng-7")
	normal("ng-7", NARRATION, "王怡钧微笑。她记得这句。她准备好了回答。", "ng-8")
	normal("ng-8", WYJ, "你不是发挥失常——你只是还没学会在紧张的时候相信自己。", "ng-9")
	normal("ng-9", LZN, "……老师，你怎么知道？我还没来得及说这些。", "ng-10")
	normal("ng-10", NARRATION, "王怡钧的笑容微微僵住。", "ng-11")
	normal("ng-11", MONOLOGUE, "（……我说得太早了。这句话，应该是几个月后才说的。）", "ng-12")
	normal("ng-12", WYJ, "我猜的。你看起来，像会那样想的人。", "ng-13")
	normal("ng-13", NARRATION, "刘子诺没有追问。但她的眼神里，有一闪而过的困惑。", "ng-14")
	normal("ng-14", WYM, "老师……好困。今天能早点结束吗？", "ng-15")
	normal("ng-15", WYJ, "可以。困的时候手别停就好。你会发现，醒着也做得出来。", "ng-16")
	normal("ng-16", WYM, "……嗯？你说得好像，已经见过我醒着做完的样子。", "ng-17")
	normal("ng-17", ZHC, "……（她安静地看着你，没有说话。）", "ng-18")
	normal("ng-18", LJD, "王老师。", "ng-19")
	normal("ng-19", LJD, "你看我们的眼神很奇怪。像是……已经认识很久了。", "ng-20")
	normal("ng-20", MONOLOGUE, "（李佳迪。你永远是最先察觉到不对劲的那一个。这一世，也一样。）", "ng-21")
	normal("ng-21", WYJ, "也许吧。就当我，对你们有些期待。", "ng-22")
	normal("ng-22", MONOLOGUE, "（这一次我知道每一个坑在哪里。我会提前把石头搬走，让她们走得更稳。连那个我自己没解开的问题，也许都能顺手解掉。）", "ng-23")
	normal("ng-23", NARRATION, "你在心里默默排好了整整几年的计划——每一次里程碑、每一次崩溃、每一句该在什么时候说的话，都按记忆里的样子摆好，像摆一盘已经知道结局的棋。", "ng-24")
	normal("ng-24", NARRATION, "四个人抬起头看你。那一刻，你以为自己握着这条路的全部答案。你还不知道，你手里那张写满答案的纸，正在被人一折、再折，慢慢折出裂痕。", null)
}

// ============================ 第二章 · 细节与人物的错位（NOIP） ============================

val NgPlusNoipScene = PlotTree.plotTree {
	entry("ng-noip-1")
	// —— 细节错位：吴一鸣的猫 ——
	normal("ng-noip-1", NARRATION, "这些周里，记忆仍然清晰，但现实开始一点点对不上。", "ng-noip-2")
	normal("ng-noip-2", WYM, "老师，今天讲完之后，可以陪我去看看年糕吗？", "ng-noip-3")
	normal("ng-noip-3", MONOLOGUE, "（我记得这段。她会给我看一张照片，年糕蜷在旧校服上。）", "ng-noip-4")
	normal("ng-noip-4", WYJ, "好。你又拍了新的照片吗？", "ng-noip-5")
	normal("ng-noip-5", WYM, "……什么照片？年糕就在楼下啊，我把它带来了。", "ng-noip-6")
	normal("ng-noip-6", WYJ, "你……把猫带来了？", "ng-noip-7")
	normal("ng-noip-7", WYM, "嗯。我怕它一个人在家太闷。", "ng-noip-8")
	normal("ng-noip-8", NARRATION, "这是记忆里没有的事。她们下楼，吴一鸣从笼子里抱出一只白色的猫。", "ng-noip-9")
	normal("ng-noip-9", MONOLOGUE, "（……年糕的眼睛，是蓝色的吗？还是黄色？）", "ng-noip-10")
	normal("ng-noip-10", MONOLOGUE, "（我记忆里所有那些照片，年糕的眼睛是什么颜色？……我想不起来。）", "ng-noip-11")
	normal("ng-noip-11", WYJ, "……它很可爱。", "ng-noip-11a")
	// —— 竞赛错位：名字叫错 ——
	normal("ng-noip-11a", NARRATION, "吴一鸣那天把年糕带来了。白色的猫装在笼子里，放在桌角。", "ng-noip-11b")
	normal("ng-noip-11b", NARRATION, "王怡钧路过的时候，低头看了一眼猫。他张口说了一句话，声音很轻，像是无意识的——", "ng-noip-11c")
	normal("ng-noip-11c", WYJ, "一一，你今天怎么来了？", "ng-noip-11d")
	normal("ng-noip-11d", WYM, "老师，你叫谁？", "ng-noip-11e")
	normal("ng-noip-11e", WYJ, "……叫它。", "ng-noip-11f")
	normal("ng-noip-11f", WYM, "它不是一一。它叫年糕。", "ng-noip-11g")
	normal("ng-noip-11g", WYJ, "……对。年糕。", "ng-noip-11h")
	normal("ng-noip-11h", NARRATION, "他走开了。吴一鸣看着他的背影，没有说话。可她低头的时候，在草稿纸上写了一个字——“一一”。那个字和她的名字没有任何关系。可她就是觉得，那个字在叫她。", "ng-noip-12")
	normal("ng-noip-12", MONOLOGUE, "（我的手指在发抖。不是因为猫。是因为我突然发现——我记得年糕的存在，却不记得年糕的样子。）", "ng-noip-12a")
	// —— 竞赛错位：题面开始模糊 ——
	normal("ng-noip-12a", NARRATION, "训练时，他翻开 NOIP 的模拟题，准备讲第三题。他记得这道题的解法——倒着推，从边界往里缩。", "ng-noip-12b")
	normal("ng-noip-12b", NARRATION, "他在白板上写下第一行条件，然后停住了。", "ng-noip-12c")
	normal("ng-noip-12c", NARRATION, "他发现自己写的那个条件，和题目对不上。不是说“写错了”。是——他写的那个条件，是属于“另一道题”的。一道他记得很清楚、但这套卷子里根本不存在的题。", "ng-noip-12d")
	normal("ng-noip-12d", MONOLOGUE, "（我站在白板前，看着自己写下的那行字，看了很久。）", "ng-noip-12e")
	normal("ng-noip-12e", LZN, "老师？", "ng-noip-12f")
	normal("ng-noip-12f", WYJ, "……没事。", "ng-noip-12g")
	normal("ng-noip-12g", NARRATION, "他把那行字擦掉，重新写了一遍。可他的手在写第二遍的时候，微微顿了一下——因为他忽然不确定，这道题的边界条件到底应该是什么了。", "ng-noip-12h")
	normal("ng-noip-12h", MONOLOGUE, "（他以前是知道的。他无比确定自己知道。可现在，他不确定了。）", "ng-noip-13")
	// —— 细节错位：朱皓辰主动 ——
	normal("ng-noip-13", NARRATION, "又一个周日。你在黑板上写图论题，转身时，朱皓辰不在座位上。", "ng-noip-14")
	normal("ng-noip-14", ZHC, "老师，你的杯子空了。我帮你去接了一杯。", "ng-noip-15")
	normal("ng-noip-15", MONOLOGUE, "（……不对。记忆里的朱皓辰，从来不会主动离开座位。她是安静地坐着、等别人发现她需要什么的人。）", "ng-noip-16")
	normal("ng-noip-16", ZHC, "老师，你怎么了？", "ng-noip-17")
	normal("ng-noip-17", WYJ, "没什么。", "ng-noip-18")
	normal("ng-noip-18", MONOLOGUE, "（我看着她回到座位。有一瞬间，我像是同时看见了两个朱皓辰——一个主动站起来的，和一个低着头、等人先开口的。记忆里的她，和眼前的她，正在分离。）", "ng-noip-19")
	// —— 细节错位：李佳迪的提问 ——
	normal("ng-noip-19", LJD, "你的课程安排得不错。下周，给我更难的问题。", "ng-noip-20")
	normal("ng-noip-20", MONOLOGUE, "（一模一样。至少李佳迪没有变。）", "ng-noip-21")
	normal("ng-noip-21", WYJ, "我会的。不过更难的题往往不在难度，在你能不能控制自己不陷进死胡同。", "ng-noip-22")
	normal("ng-noip-22", LJD, "老师，你是不是认识我？", "ng-noip-23")
	normal("ng-noip-23", WYJ, "……什么意思？", "ng-noip-24")
	normal("ng-noip-24", LJD, "你看我的方式，像在看一个你早就认识的人。不对——像在看一个你以前教过的学生。", "ng-noip-25")
	normal("ng-noip-25", LJD, "老师，你以前教过别的学生吗？", "ng-noip-26")
	normal("ng-noip-26", WYJ, "没有。你们是我第一批学生。", "ng-noip-27")
	normal("ng-noip-27", LJD, "那你为什么看我的时候，像在看什么人？", "ng-noip-27a")
	// —— 竞赛错位：排名表名字重叠 ——
	normal("ng-noip-27a", NARRATION, "CSP-S 成绩公布那天，他看了一眼排名表。四个名字排在一起，和一周目一样，顺序都没变。", "ng-noip-27b")
	normal("ng-noip-27b", NARRATION, "可他的目光落上去的时候，他看见了一个不该出现的东西——", "ng-noip-27c")
	normal("ng-noip-27c", NARRATION, "李佳迪的名字旁边，有一行淡淡的、几乎看不出的灰色字迹。像是有人用很轻的笔写过，又擦掉了，可印痕还在。", "ng-noip-27d")
	normal("ng-noip-27d", NARRATION, "那行字写的是：吴一鸣。", "ng-noip-27e")
	normal("ng-noip-27e", NARRATION, "他揉了揉眼睛。再看的时候，那行字已经没有了。", "ng-noip-27f")
	normal("ng-noip-27f", MONOLOGUE, "（你刚才看见的，不是幻觉。）", "ng-noip-choice")
	// 玩家的回答无法改变李佳迪的洞察——三条分支都汇回同一条主线。
	option("ng-noip-choice", "面对李佳迪的追问，你怎么回答？", listOf(
		"（凭记忆抢答）你是李佳迪，永远的第一。" to "ng-noip-a1",
		"我只是觉得你，很像一个人。" to "ng-noip-b1",
		"（沉默，不回答。）" to "ng-noip-c1"
	))
	normal("ng-noip-a1", WYJ, "你是李佳迪。永远的第一。你不会主动帮别人，你只会等别人来问。", "ng-noip-a2")
	normal("ng-noip-a2", LJD, "……我经常帮朱皓辰。你以前也看过。你到底认识哪个我？", "ng-noip-merge")
	normal("ng-noip-b1", WYJ, "……我只是觉得你，很像一个人。", "ng-noip-b2")
	normal("ng-noip-b2", LJD, "谁？", "ng-noip-b3")
	normal("ng-noip-b3", WYJ, "一个……我还没认识的人。", "ng-noip-b4")
	normal("ng-noip-b4", NARRATION, "这句话出口后，你自己也愣住了。它没有意义——但它是你此刻唯一能说出的真话。", "ng-noip-merge")
	normal("ng-noip-c1", NARRATION, "你没有回答。李佳迪看了你很久，没有再追问。", "ng-noip-c2")
	normal("ng-noip-c2", LJD, "你不用说。我大概知道了——你看的不是我。", "ng-noip-merge")
	// —— 人物混淆：对朱皓辰说了李佳迪的话 ——
	normal("ng-noip-merge", NARRATION, "NOIP 结束后的一个课后，你单独找到朱皓辰。你记得她需要听到一句关于“稳定不代表不允许脆弱”的话。", "ng-noip-28")
	normal("ng-noip-28", WYJ, "朱皓辰，你不必永远是最强的那个。你不需要用第一，来证明自己的价值。", "ng-noip-29")
	normal("ng-noip-29", ZHC, "老师，我没想成为最强的那个。那是李佳迪的事。我只想做好自己的事，不犯错就行。", "ng-noip-30")
	normal("ng-noip-30", MONOLOGUE, "（……我搞混了。我刚才那句话，是第一世第二周对李佳迪说的。我把李佳迪的恐惧，安在了朱皓辰身上。）", "ng-noip-31")
	normal("ng-noip-31", WYJ, "对不起。我可能……记错了。", "ng-noip-32")
	normal("ng-noip-32", NARRATION, "朱皓辰没有追究，点点头就离开了。你站在原地，突然分不清——你记得的朱皓辰，是“想被看见却不敢说”的那个，还是“不想被看见、却正在学着接受”的那个？", "ng-noip-33")
	normal("ng-noip-33", MONOLOGUE, "（我记得她们每个人需要什么。但我开始不记得——哪一份需要，属于哪一个人了。）", "ng-noip-end")
	normal("ng-noip-end", NARRATION, "成绩公布，四个人都过了 NOIP。你按记忆写好了庆祝的话，可说出口的时候，你已经不确定这句话原本是准备给谁的了。", null)
}

// ============================ 第三章前 · 现实的渗透（省队前夜） ============================

val NgPlusBeforeTeamScene = PlotTree.plotTree {
	entry("ng-team-1")
	// —— 幻觉开始渗透：白板上不是她写的字 ——
	normal("ng-team-1", NARRATION, "省队选拔临近。教室里气氛很好，刘子诺在大声说着什么，吴一鸣在笑，朱皓辰点头，李佳迪站在窗边。一切都很正常。", "ng-team-2")
	normal("ng-team-2", NARRATION, "可你站在讲台上时，看见教室最后面——靠着墙，站着“你自己”。", "ng-team-3")
	normal("ng-team-3", MONOLOGUE, "（另一个王怡钧。穿着一件我从没见过的衣服，不属于这几年里任何一年。她没有表情，只是看着我。）", "ng-team-4")
	normal("ng-team-4", NARRATION, "你转头看其他人——没有人看向那个方向。你再回头，那个“自己”已经不见了。", "ng-team-5")
	normal("ng-team-5", NARRATION, "白板上却出现了一行字，不是你写的：「你再试多少次，都改变不了结局。」", "ng-team-6")
	normal("ng-team-6", NARRATION, "你走过去，用板擦把那行字抹掉。刘子诺还在说话，没有人注意到——或者，他们看不见。", "ng-team-7")
	// —— 混淆到顶点：分不清谁是谁 ——
	normal("ng-team-7", NARRATION, "四个女孩围过来讨论省队名额。你记得，你应该在这一晚说一段话，关于“名额有限，但不代表任何人的价值有限”。", "ng-team-8")
	normal("ng-team-8", MONOLOGUE, "（可我张开嘴，说出来的不是那段话。）", "ng-team-9")
	normal("ng-team-9", WYJ, "……你们知道吗，我有时候，分不清你们谁是谁了。", "ng-team-10")
	normal("ng-team-10", NARRATION, "教室里安静下来。", "ng-team-11")
	normal("ng-team-11", LZN, "老师……你在开玩笑吧？", "ng-team-12")
	normal("ng-team-12", WYJ, "……不是玩笑。", "ng-team-13")
	normal("ng-team-13", WYJ, "我记得你们的每一件事。但我分不清哪件事属于谁了。我好像……把你们放在同一个地方，然后标签，都掉了。", "ng-team-14")
	normal("ng-team-14", NARRATION, "没有人说话。空气沉重得像凝固的墨水。", "ng-team-15")
	normal("ng-team-15", ZHC, "老师，那你先不叫我们的名字。你只要看着我们，就好。", "ng-team-16")
	normal("ng-team-16", MONOLOGUE, "（记忆里的朱皓辰是“话不多、但每次开口都准”的人。可这一世，她在主动告诉我——该怎么对待她。）", "ng-team-17")
	normal("ng-team-17", WYJ, "……好。我先看着。", "ng-team-18")
	normal("ng-team-18", NARRATION, "你看着她们，她们也看着你。那一刻没有排名、没有比赛、没有“你应该做什么”的预设——只有五个人，在互相看。", "ng-team-19")
	normal("ng-team-19", MONOLOGUE, "（四张脸在我眼里交替闪回——现实中的脸，和记忆中的脸，反复重叠、错位、又分离。我拼命想把它们对齐，可怎么都对不上。）", null)
}

// ============================ 终章前 · 幻觉的顶点（NOI 前夜） ============================

val NgPlusBeforeNoiScene = PlotTree.plotTree {
	entry("ng-noi-eve-1")
	normal("ng-noi-eve-1", NARRATION, "NOI 前夜。四个人坐在教室里，你站在白板前，准备说一些关于“明天只需要把会做的做完”的话。", "ng-noi-eve-2")
	normal("ng-noi-eve-2", WYJ, "我好像……已经把你们带到了我能带的最远的地方。", "ng-noi-eve-3")
	normal("ng-noi-eve-3", MONOLOGUE, "（……这句话我上一世说过了。我应该换一句，我明明准备过不同的。）", "ng-noi-eve-4")
	normal("ng-noi-eve-4", MONOLOGUE, "（可我努力回想“准备过”的话，只想起更多碎片——第一世的对话、这一世的对话，还有一些我自己都不确定是否真的发生过的对话。）", "ng-noi-eve-5")
	normal("ng-noi-eve-5", NARRATION, "你张了张嘴。没有声音。然后你闭上眼睛。", "ng-noi-eve-6")
	normal("ng-noi-eve-6", NARRATION, "当你再睁开时，四个女孩都不在座位上了。她们站在门口，背对着你。", "ng-noi-eve-7")
	normal("ng-noi-eve-7", LJD, "老师，我们明天会进考场的。", "ng-noi-eve-8")
	normal("ng-noi-eve-8", LZN, "你不用担心我们。", "ng-noi-eve-9")
	normal("ng-noi-eve-9", WYM, "我们会好好的。", "ng-noi-eve-10")
	normal("ng-noi-eve-10", ZHC, "你也是。", "ng-noi-eve-11")
	normal("ng-noi-eve-11", NARRATION, "她们没有回头，走出了教室。你一个人站在空教室里。", "ng-noi-eve-12")
	normal("ng-noi-eve-12", WYJ, "……她们走了吗？", "ng-noi-eve-13")
	normal("ng-noi-eve-13", NARRATION, "白板上出现一行字，不是你写的：「是的。你已经走到这里了。」", "ng-noi-eve-14")
	normal("ng-noi-eve-14", WYJ, "……我该做什么？", "ng-noi-eve-15")
	normal("ng-noi-eve-15", NARRATION, "白板上的字消失了。没有回答。", null)
}

// ============================ 终章 · 我记不清她们了（NOI 结局） ============================

val NgPlusNoiScene = PlotTree.plotTree {
	entry("ng-noi-1")
	normal("ng-noi-1", NARRATION, "NOI 的赛场之外。阳光很好。成绩已经出来了——四个人，没有一个人站上金牌线。", "ng-noi-1b")
	normal("ng-noi-1b", NARRATION, "你带着上一世的全部记忆重来，却连一枚金牌都没能替她们留住。", "ng-noi-2")
	normal("ng-noi-2", NARRATION, "可你站在她们面前时，看着四张脸——你记得每张脸的名字。但你叫不出来。", "ng-noi-3")
	normal("ng-noi-3", MONOLOGUE, "（我记得她们所有的故事。但我说不出，哪个故事属于谁。）", "ng-noi-4")
	normal("ng-noi-4", NARRATION, "四张脸在你眼中变成模糊的色块——不对，又变回清晰。又模糊了。", "ng-noi-5")
	normal("ng-noi-5", LJD, "老师，你还好吗？", "ng-noi-6")
	normal("ng-noi-6", NARRATION, "你抬起头，看着她。", "ng-noi-7")
	normal("ng-noi-7", WYJ, "……你是李佳迪。", "ng-noi-8")
	normal("ng-noi-8", LJD, "对。", "ng-noi-9")
	normal("ng-noi-9", WYJ, "你是……最强的那个人。你说过，你不想永远只做第一。", "ng-noi-10")
	normal("ng-noi-10", LJD, "那是很久以前说的了。我已经改了很多。你不记得了？", "ng-noi-11")
	normal("ng-noi-11", NARRATION, "你没有回答。你看着另外三个人。你记得她们的名字。但你说不出，哪张脸是“吴一鸣”，哪张是“刘子诺”，哪张是“朱皓辰”。", "ng-noi-12")
	normal("ng-noi-12", NARRATION, "名字和面孔像拼图碎片一样散落。你试图拼合，但每一块，都不匹配。", "ng-noi-13")
	normal("ng-noi-13", MONOLOGUE, "（我记得她们。我记得她们每一个人。但我不记得，谁是谁了。）", "ng-noi-14")
	normal("ng-noi-14", NARRATION, "四周的声响忽然远去。寂静里，有什么字慢慢浮了上来——", "ng-noi-15")
	normal("ng-noi-15", NARRATION, "「我记得她们。」", "ng-noi-16")
	normal("ng-noi-16", NARRATION, "「但我记不清她们了。」", "ng-noi-17")
	normal("ng-noi-17", NARRATION, "「如果有一天我忘了她们是谁——她们，还会记得我吗？」", "ng-noi-18")
	normal("ng-noi-18", NARRATION, "没有回答。", "ng-noi-19")
	// —— 尾声：空教室 ——
	normal("ng-noi-19", NARRATION, "最后是一间空教室的俯视。四把椅子并排放着，没有人坐在上面。", "ng-noi-20")
	normal("ng-noi-20", NARRATION, "安静了很久——然后，其中一把椅子微微移动了一下，像是有人刚刚站起来，只是已经走远了。", "ng-noi-21")
	normal("ng-noi-21", NARRATION, "然后，一切归于寂静。", null)
}
