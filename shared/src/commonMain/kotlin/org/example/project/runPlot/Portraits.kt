package org.example.project.runPlot

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.project.plotsFormat.Characters
import org.jetbrains.compose.resources.decodeToImageBitmap
import wyjgalgame.shared.generated.resources.Res

/**
 * 角色立绘。
 *
 * 设计目标：**有图就显示，没图就当无事发生**。这样你可以先手绘几张简陋的占位图，
 * 逐个丢进资源目录，游戏立刻显示；还没画的角色不会报错、不会崩溃，退化成原来的纯文字表现。
 *
 * ## 怎么加立绘（给美术/你自己看）
 * 1. 把 PNG（**透明背景**、竖版人物）放进：
 *    `shared/src/commonMain/composeResources/drawable/`
 * 2. 文件名与下面 [resourcePathFor] 里登记的路径一致即可（大小写、连字符都可以，
 *    因为这里是按路径字符串读取字节，不依赖生成的资源访问器）。当前登记：
 *    - 王怡钧 → `portrait_wyj.png`
 *    - 李佳迪 → `portrait_ljd.png`
 *    - 刘子诺 → `LZN-CALM.png`
 *    - 吴一鸣 → `WYM-SLEEPY.png`
 *    - 朱皓辰 → `portrait_zhc.png`
 * 3. 若换了文件名，改 [resourcePathFor] 里对应那一行即可。
 * 4. 重新运行游戏即可。旁白/独白（NARRATION、MONOLOGUE）不显示立绘。
 *
 * 建议尺寸：约 800×1200 或更高，竖版；人物大致居中、底部留一点空白，
 * 因为立绘会贴着对话框上沿、按高度铺满显示。
 */
object Portraits {
    /**
     * 把学生存档 id（如 "WYM"）映射到 [Characters]，用于在经营面板、结营小结等处显示头像。
     * 学生 id 与 [Characters] 的枚举名一致；无法匹配（如未来新增角色）时返回 null。
     */
    fun characterForStudentId(studentId: String): Characters? =
        Characters.entries.firstOrNull { it.name == studentId }

    /** 说话人 → 立绘资源相对路径（相对于 composeResources 根）。旁白/独白没有立绘。 */
    private fun resourcePathFor(speaker: Characters): String? = when (speaker) {
        Characters.WYJ -> "drawable/portrait_wyj.png"
        Characters.LJD -> "drawable/portrait_ljd.png"
        Characters.LZN -> "drawable/LZN-CALM.png"
        Characters.WYM -> "drawable/WYM-SLEEPY.png"
        Characters.ZHC -> "drawable/portrait_zhc.png"
        Characters.NARRATION, Characters.MONOLOGUE -> null
    }

    /**
     * 说话人 → 头像资源相对路径。头像是从立绘裁出的头部方图（正方形、透明背景），
     * 用于历史回看、存档位、成就墙等需要小图标的地方。缺图时同样静默降级。
     */
    private fun avatarPathFor(speaker: Characters): String? = when (speaker) {
        Characters.WYJ -> "drawable/WYJ-AVATAR.png"
        Characters.LJD -> "drawable/LJD-AVATAR.png"
        Characters.LZN -> "drawable/LZN-AVATAR.png"
        Characters.WYM -> "drawable/WYM-AVATAR.png"
        Characters.ZHC -> "drawable/ZHC-AVATAR.png"
        Characters.NARRATION, Characters.MONOLOGUE -> null
    }

    // 已解码的立绘缓存，避免每次重组都重新读盘解码。
    // 值为 null 表示“确认过、没有这张图”，据此跳过重复的读取尝试。
    private val cache = mutableMapOf<Characters, ImageBitmap?>()

    // 头像缓存，与立绘缓存分开。
    private val avatarCache = mutableMapOf<Characters, ImageBitmap?>()

    /**
     * 读取并解码某个角色的立绘；找不到文件（或该角色本就无立绘）时返回 null。
     * 用 [Res.readBytes] 在运行时按路径取字节，因此**缺图不会导致编译或运行失败**。
     */
    private suspend fun load(speaker: Characters): ImageBitmap? {
        if (cache.containsKey(speaker)) return cache[speaker] // 之前已确认（有图或无图）。
        val path = resourcePathFor(speaker) ?: run {
            cache[speaker] = null
            return null
        }
        val bitmap = try {
            Res.readBytes(path).decodeToImageBitmap()
        } catch (_: Throwable) {
            // 文件不存在或解码失败：安静降级为“无立绘”。
            null
        }
        cache[speaker] = bitmap
        return bitmap
    }

    /** 读取并解码头像；规则同 [load]，缺图返回 null。 */
    private suspend fun loadAvatar(speaker: Characters): ImageBitmap? {
        if (avatarCache.containsKey(speaker)) return avatarCache[speaker]
        val path = avatarPathFor(speaker) ?: run {
            avatarCache[speaker] = null
            return null
        }
        val bitmap = try {
            Res.readBytes(path).decodeToImageBitmap()
        } catch (_: Throwable) {
            null
        }
        avatarCache[speaker] = bitmap
        return bitmap
    }

    /**
     * 当前说话人的立绘展示位。放在对话框后面、靠底部对齐。
     * 无图时整块不占位、不绘制，界面与原来完全一致。
     */
    @Composable
    fun SpeakerPortrait(speaker: Characters, modifier: Modifier = Modifier) {
        var bitmap by remember(speaker) { mutableStateOf(cache[speaker]) }
        LaunchedEffect(speaker) {
            bitmap = load(speaker)
        }
        val image = bitmap ?: return
        Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
            Image(
                painter = BitmapPainter(image),
                contentDescription = "${speaker.displayName}立绘",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxHeight().fillMaxWidth()
            )
        }
    }

    /**
     * 说话人的圆形头像。适合放在历史回看每句台词旁、存档位、成就墙等处。
     * 缺头像时返回 false（不绘制任何东西），方便调用方决定是否退回文字/留白。
     *
     * @return 是否绘制了头像（有图为 true）。
     */
    @Composable
    fun SpeakerAvatar(
        speaker: Characters,
        size: Dp = 40.dp,
        showBorder: Boolean = true,
        modifier: Modifier = Modifier,
    ): Boolean {
        var bitmap by remember(speaker) { mutableStateOf(avatarCache[speaker]) }
        LaunchedEffect(speaker) {
            bitmap = loadAvatar(speaker)
        }
        val image = bitmap ?: return false
        val shaped = modifier.size(size).clip(CircleShape).let {
            if (showBorder) it.border(1.dp, Color(0x33000000), CircleShape) else it
        }
        Image(
            painter = BitmapPainter(image),
            contentDescription = "${speaker.displayName}头像",
            contentScale = ContentScale.Crop,
            modifier = shaped
        )
        return true
    }
}
