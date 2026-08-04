package com.eric.wandroid.ui.moyu.wallpaper

data class WallpaperCategory(
    val label: String,
    val id: String
)

data class WallpaperCategoryGroup(
    val label: String,
    val categories: List<WallpaperCategory>
)

object WallpaperCategoryCatalog {
    val groups: List<WallpaperCategoryGroup> = listOf(
        WallpaperCategoryGroup(
            label = "三次元综合",
            categories = listOf(
                WallpaperCategory("小姐姐", "xjj"),
                WallpaperCategory("高清风景壁纸", "gqbz"),
                WallpaperCategory("猫星人", "cat")
            )
        ),
        WallpaperCategoryGroup(
            label = "二次元综合",
            categories = listOf(
                WallpaperCategory("横屏壁纸", "pc"),
                WallpaperCategory("竖屏壁纸", "sjpic"),
                WallpaperCategory("图片一", "img1"),
                WallpaperCategory("图片二", "img2"),
                WallpaperCategory("黑丝", "acghs"),
                WallpaperCategory("白丝", "acgbs"),
                WallpaperCategory("兽耳", "kemonomimi"),
                WallpaperCategory("白毛", "yin"),
                WallpaperCategory("星空", "xingk"),
                WallpaperCategory("loli", "loli")
            )
        ),
        WallpaperCategoryGroup(
            label = "动漫系列",
            categories = listOf(
                WallpaperCategory("赛马娘", "saima"), WallpaperCategory("RE0", "re0"),
                WallpaperCategory("SAO", "sao"), WallpaperCategory("妖精的尾巴", "yaowei"),
                WallpaperCategory("鬼灭之刃", "gmzr"), WallpaperCategory("五等分的花嫁", "5huajia"),
                WallpaperCategory("冰菓", "bingg"), WallpaperCategory("你的名字", "kiminame"),
                WallpaperCategory("公主连接", "gongzhulj"), WallpaperCategory("间谍过家家", "spyfamily"),
                WallpaperCategory("摇曳露营", "camp"), WallpaperCategory("摇曳百合", "yuruyuri"),
                WallpaperCategory("天使降临到我身边", "miyone"), WallpaperCategory("我们无法一起学习", "xuebulai"),
                WallpaperCategory("悠哉日常大王", "nobiyori"), WallpaperCategory("黄金拼图", "kin-iro-mosaic"),
                WallpaperCategory("转生恶役大小姐", "flag-ojousama"), WallpaperCategory("堀与宫村", "hori-to-miyamura"),
                WallpaperCategory("路人女主", "saenai-heroine"), WallpaperCategory("喜欢本大爷竟然就你一个", "mydcy"),
                WallpaperCategory("高原魔女", "slime-300"), WallpaperCategory("幼妻狐仙", "fox-senko"),
                WallpaperCategory("游戏王", "yu-gi-oh"), WallpaperCategory("莉可丽丝", "lycoris-recoil"),
                WallpaperCategory("斩.赤红之瞳", "akame-ga-kill"), WallpaperCategory("Fgo", "fgo"),
                WallpaperCategory("轻音", "k-on"), WallpaperCategory("Lovelive", "lovelive"),
                WallpaperCategory("Overlord[骨王]", "overlord"), WallpaperCategory("变态王子与不笑猫", "hentaiandneko"),
                WallpaperCategory("不正经魔术师", "majutsu-koushi"), WallpaperCategory("空之境界", "kara-no-kyoukai"),
                WallpaperCategory("小林家的龙女仆", "kobayashi-no-dragon"), WallpaperCategory("龙与虎", "toradora"),
                WallpaperCategory("关于我转生变成史莱姆这件事", "tensei-slime"), WallpaperCategory("未闻花名", "hana-no-amae"),
                WallpaperCategory("乌贼娘", "ika-usume"), WallpaperCategory("小老师", "celia-claire"),
                WallpaperCategory("熊熊勇闯异世界", "kuma-bear"), WallpaperCategory("为美好的世界献上祝福", "sekai-shukufuku")
            )
        ),
        WallpaperCategoryGroup(
            label = "游戏系列",
            categories = listOf(
                WallpaperCategory("原神", "ys"), WallpaperCategory("明日方舟", "mrfz"),
                WallpaperCategory("碧蓝航线", "blhx"), WallpaperCategory("车万", "dongf"),
                WallpaperCategory("碧蓝档案", "blda"), WallpaperCategory("缘之空", "yzk"),
                WallpaperCategory("少女前线", "snqx"), WallpaperCategory("崩坏三", "bh3")
            )
        ),
        WallpaperCategoryGroup(
            label = "虚拟主播系列",
            categories = listOf(
                WallpaperCategory("小鲨鱼", "gawr-gura"), WallpaperCategory("雪花菈米", "yukihana"),
                WallpaperCategory("夏色祭", "natsuiro"), WallpaperCategory("润羽露西娅", "uruha-rushia"),
                WallpaperCategory("花园Serena", "hanazono-serena"), WallpaperCategory("笹木咲", "sasaki-saku"),
                WallpaperCategory("角卷绵芽", "tsunomaki-watame"), WallpaperCategory("常暗永远", "tokoyami-towa"),
                WallpaperCategory("天宫心", "amamiya-kokoro"), WallpaperCategory("兔田佩克菈", "usada-pekora"),
                WallpaperCategory("一伊那尔栖", "ninomae"), WallpaperCategory("大神澪", "ookami-mio"),
                WallpaperCategory("星川莎拉", "sara-hoshikawa"), WallpaperCategory("樱巫女", "sakura-miko"),
                WallpaperCategory("Hololive EN", "holoen"), WallpaperCategory("绊爱", "kizunaai"),
                WallpaperCategory("神乐七奈", "kagura-nana"), WallpaperCategory("神乐Mea", "kagura-mea"),
                WallpaperCategory("白上吹雪", "fubuki"), WallpaperCategory("戌神沁音", "inugami-korone"),
                WallpaperCategory("阿夸", "aqua"), WallpaperCategory("猫宫日向", "nekomiya-hinata")
            )
        ),
        WallpaperCategoryGroup(
            label = "表情包系列",
            categories = listOf(
                WallpaperCategory("二次元表情包", "bqb"), WallpaperCategory("甘城猫猫", "gcmm"),
                WallpaperCategory("MC酱", "mc"), WallpaperCategory("kemomimi兽耳酱", "kemomimi"),
                WallpaperCategory("三次元猫猫", "miao"), WallpaperCategory("阿夸", "akqa"),
                WallpaperCategory("柴郡猫猫", "cheshire"), WallpaperCategory("猫猫虫咖波", "capoo"),
                WallpaperCategory("塞西莉亚（黑白）", "ceciliabqb"), WallpaperCategory("塞西莉亚", "cecilia"),
                WallpaperCategory("龙图", "longtu"), WallpaperCategory("罗翔", "luox"),
                WallpaperCategory("滑稽", "huaji"), WallpaperCategory("熊猫头", "pand"),
                WallpaperCategory("同福客栈", "tfkz"), WallpaperCategory("原神", "ysbqb"),
                WallpaperCategory("北方酱", "beifang"), WallpaperCategory("柯南", "kenan"),
                WallpaperCategory("猫和老鼠", "tomandjerry"), WallpaperCategory("小白人", "whitevillain"),
                WallpaperCategory("小婳", "xiaohua"), WallpaperCategory("商业头盔", "toukui"),
                WallpaperCategory("牢饭降临到我身边", "laofan"), WallpaperCategory("狗妈", "goum"),
                WallpaperCategory("索菲", "sofei"), WallpaperCategory("滑稽嘤嘤嘤", "yingyy"),
                WallpaperCategory("草莓果酱", "caomeiguo"), WallpaperCategory("和泉纱雾", "shawu")
            )
        ),
        WallpaperCategoryGroup(
            label = "角色系列",
            categories = listOf(
                WallpaperCategory("猫羽雫", "myn"), WallpaperCategory("樱岛麻衣", "ydmy"),
                WallpaperCategory("初音未来", "miku"), WallpaperCategory("洛天依", "tianyi"),
                WallpaperCategory("五更琉璃", "gokou-ruri"), WallpaperCategory("椎名真白", "mashiro"),
                WallpaperCategory("鹿乃", "kano"), WallpaperCategory("Saber", "saber"),
                WallpaperCategory("四系乃", "yoshino"), WallpaperCategory("见崎鸣", "misakimei"),
                WallpaperCategory("阿卡林", "akari"), WallpaperCategory("康娜", "kanna"),
                WallpaperCategory("喵帕斯", "miaops"), WallpaperCategory("妮姆芙", "nymph"),
                WallpaperCategory("诺艾尔", "noel"), WallpaperCategory("时崎狂三", "kurumi"),
                WallpaperCategory("薇尔莉特", "violet"), WallpaperCategory("忍野忍", "shinobu"),
                WallpaperCategory("风见一姬", "kazuki"), WallpaperCategory("伊莉雅", "iliya"),
                WallpaperCategory("碧翠丝", "beatrice"), WallpaperCategory("土间埋", "umr"),
                WallpaperCategory("雷姆", "rem"), WallpaperCategory("阿波连", "aharen"),
                WallpaperCategory("国家队02", "02"), WallpaperCategory("阿尼亚", "aniya"),
                WallpaperCategory("高木", "takagi"), WallpaperCategory("御坂美琴", "misaka-mikoto"),
                WallpaperCategory("约尔", "yor"), WallpaperCategory("水原千鹤", "mizuhara"),
                WallpaperCategory("矢泽妮可", "nico"), WallpaperCategory("唐可可", "tangkk"),
                WallpaperCategory("千反田爱瑠", "eru"), WallpaperCategory("亚丝娜", "asuna"),
                WallpaperCategory("香风智乃", "chiro"), WallpaperCategory("凯露", "karyl"),
                WallpaperCategory("灰原哀", "haibara"), WallpaperCategory("雏鹤爱", "hinatsuru"),
                WallpaperCategory("志摩凛", "shimarin"), WallpaperCategory("小鸟游六花", "rikka"),
                WallpaperCategory("加藤惠", "katoumegumi"), WallpaperCategory("雪之下雪乃", "yukino"),
                WallpaperCategory("谢丝塔", "siesta"), WallpaperCategory("早坂爱", "hayasakaai"),
                WallpaperCategory("四宫辉夜", "kaguya"), WallpaperCategory("凉宫春日", "haruhi"),
                WallpaperCategory("藤原千花", "chika"), WallpaperCategory("祢豆子", "nezuko"),
                WallpaperCategory("小野寺小咲", "onoderaoosaki"), WallpaperCategory("中野三玖", "nakanomiku"),
                WallpaperCategory("伊蕾娜", "elaina"), WallpaperCategory("佐天泪子", "ruiko"),
                WallpaperCategory("白井黑子", "kuroko"), WallpaperCategory("泉此方", "konata"),
                WallpaperCategory("白银圭", "shiroganekei"), WallpaperCategory("伊井野弥子", "linomiko"),
                WallpaperCategory("立华奏", "kanade"), WallpaperCategory("喜多川海梦", "kitagawa-marin"),
                WallpaperCategory("熊污女[雨宿町]", "amayadori-machi"), WallpaperCategory("牧濑红莉栖", "makise-kurisu"),
                WallpaperCategory("艾拉", "lsla"), WallpaperCategory("蝶祈", "yuzuriha-inori"),
                WallpaperCategory("伊卡洛斯", "uranus-queen"), WallpaperCategory("八寻宁宁", "yashiro-nene"),
                WallpaperCategory("菲洛", "filo"), WallpaperCategory("食蜂操祈", "shokuho-isaki"),
                WallpaperCategory("我妻由乃", "gasai-yuno"), WallpaperCategory("长瀞同学", "nagatoro-hayase"),
                WallpaperCategory("蜘蛛子", "noname-kumo"), WallpaperCategory("和泉纱雾", "izumi-sagiri"),
                WallpaperCategory("栗山未来", "kuriyama-mirai"), WallpaperCategory("奈亚子", "nyaruko"),
                WallpaperCategory("沙优", "ogiwara-sayu"), WallpaperCategory("维包子", "blois"),
                WallpaperCategory("摘希", "miniwa-tsumiki")
            )
        )
    )
}
