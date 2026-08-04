# MoeHu 壁纸 API 清单

> 来源：<https://img.moehu.org/index.php>  
> 整理日期：2026-08-01  
> 接口基址：`https://img.moehu.org/`

## 概览

这是一个无需认证的随机图片接口。它不提供关键词搜索、分页或图片详情查询；调用方通过指定分类 `id` 获取随机图片。首页当前公开 7 个图片分类、186 个图片 ID，以及 3 个文字 ID。

站点首页标记的 API 最后更新时间是 `2022-07-31`，分类、图片数量、CDN 与可用性都可能随时变化。以下清单以整理当日首页内容为准。

## 图片接口

### 1. 随机图片 / JSON

`GET https://img.moehu.org/pic.php`

默认直接返回或重定向到一张随机图片。加上 `return=json` 可取得图片 URL 的 JSON 响应。

| 参数 | 可选值 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `id` | 下方图片分类 ID | `img1` | 图片分类 |
| `size` | `large`、`mw1024`、`mw690`、`bmiddle`、`small`、`thumb180`、`thumbnail`、`square` | `large` | 图片规格 |
| `num` | `0`-`100` | `1` | 仅 JSON 模式有效，请求数量 |
| `return` | `json` 或省略 | 省略 | 省略时返回图片；`json` 时返回 JSON |
| `cdn` | `baidu`、`wp`、`cf`、`lo`、`vercel` | `baidu` | CDN 分流 |
| `yuan` | `sina`、`sm` | `sina` | 图源参数，站点注明“暂未启用” |

#### 已验证示例

单张 JSON：

```text
GET https://img.moehu.org/pic.php?return=json&id=img1&num=1
```

实测响应结构：

```json
{
  "code": "200",
  "acgurl": "https://imgcdn.xn--b9wn8umwv.com/large/....jpg",
  "category": "img1"
}
```

多张 JSON：

```text
GET https://img.moehu.org/pic.php?return=json&id=img1&num=2
```

实测响应结构：

```json
{
  "code": "200",
  "pic": [
    "https://imgcdn.xn--b9wn8umwv.com/large/....jpg",
    "https://imgcdn.xn--b9wn8umwv.com/large/....jpg"
  ],
  "count": 2,
  "category": "img1"
}
```

指定尺寸与 CDN：

```text
GET https://img.moehu.org/pic.php?id=img1&cdn=cf&size=mw1024
```

### 2. 图片预览页

`GET https://img.moehu.org/pics.php`

用于浏览指定分类的图片，主要面向人工预览，不建议作为应用图片数据接口。

```text
https://img.moehu.org/pics.php?id=img1&size=small
```

支持的公开参数：`id`、`size`。

## 文字接口

`GET https://img.moehu.org/txt/?id={id}`

返回纯文本或 HTML 文本内容。实测 `id=dm` 返回 `200` 和文本内容；响应头为 `text/html;charset=utf8`，客户端应按文本读取并做好乱码容错。

| 文本类型 | ID | 首页标注数量 | 示例 |
| --- | --- | ---: | --- |
| 随机动漫台词 | `dm` | 506 | `https://img.moehu.org/txt/?id=dm` |
| 随机舔狗日记 | `tiangou` | 131 | `https://img.moehu.org/txt/?id=tiangou` |
| 随机唐诗 | `tangshi` | 920 | `https://img.moehu.org/txt/?id=tangshi` |

## 辅助页面

| 地址 | 用途 | 是否建议作为应用 API |
| --- | --- | --- |
| `https://img.moehu.org/index.php` | 接口首页和分类清单 | 否，适合人工查看 |
| `https://img.moehu.org/pics.php` | 图片预览 | 否，适合人工查看 |
| `https://img.moehu.org/stats.php` | 调用和 ID 统计页面 | 否，统计展示页 |
| `https://img.moehu.org/user/add.php` | 自助提交图片 ID | 否，管理页面 |

## 图片分类 ID

数量为首页展示值，仅作选型参考；请求时真正需要的是 ID。

### 三次元综合

| 名称 | ID | 数量 |
| --- | --- | ---: |
| 小姐姐 | `xjj` | 1,436 |
| 高清风景壁纸 | `gqbz` | 137 |
| 猫星人 | `cat` | 1,947 |

### 二次元综合

| 名称 | ID | 数量 |
| --- | --- | ---: |
| 图片一 | `img1` | 849 |
| 图片二 | `img2` | 78 |
| 竖屏壁纸 | `sjpic` | 568 |
| 横屏壁纸 | `pc` | 440 |
| 黑丝 | `acghs` | 117 |
| 白丝 | `acgbs` | 244 |
| 兽耳 | `kemonomimi` | 510 |
| 白毛 | `yin` | 397 |
| 星空 | `xingk` | 299 |
| loli | `loli` | 235 |

### 动漫系列

| 名称 | ID | 数量 |
| --- | --- | ---: |
| 赛马娘 | `saima` | 445 |
| RE0 | `re0` | 20 |
| SAO | `sao` | 19 |
| 妖精的尾巴 | `yaowei` | 17 |
| 鬼灭之刃 | `gmzr` | 21 |
| 五等分的花嫁 | `5huajia` | 152 |
| 冰菓 | `bingg` | 204 |
| 你的名字 | `kiminame` | 175 |
| 公主连接 | `gongzhulj` | 195 |
| 间谍过家家 | `spyfamily` | 135 |
| 摇曳露营 | `camp` | 150 |
| 摇曳百合 | `yuruyuri` | 164 |
| 天使降临到我身边 | `miyone` | 0 |
| 我们无法一起学习 | `xuebulai` | 75 |
| 悠哉日常大王 | `nobiyori` | 120 |
| 黄金拼图 | `kin-iro-mosaic` | 94 |
| 转生恶役大小姐 | `flag-ojousama` | 73 |
| 崛与宫村 | `hori-to-miyamura` | 203 |
| 路人女主 | `saenai-heroine` | 140 |
| 喜欢本大爷竟然就你一个 | `mydcy` | 76 |
| 高原魔女 | `slime-300` | 172 |
| 幼妻狐仙 | `fox-senko` | 77 |
| 游戏王 | `yu-gi-oh` | 231 |
| 莉可丽丝 | `lycoris-recoil` | 126 |
| 斩.赤红之瞳 | `akame-ga-kill` | 32 |
| Fgo | `fgo` | 86 |
| 轻音 | `k-on` | 132 |
| Lovelive | `lovelive` | 211 |
| Overlord[骨王] | `overlord` | 94 |
| 变态王子与不笑猫 | `hentaiandneko` | 94 |
| 不正经魔术师 | `majutsu-koushi` | 52 |
| 空之境界 | `kara-no-kyoukai` | 54 |
| 小林家的龙女仆 | `kobayashi-no-dragon` | 103 |
| 龙与虎 | `toradora` | 90 |
| 关于我转生变成史莱姆这件事 | `tensei-slime` | 132 |
| 未闻花名 | `hana-no-amae` | 96 |
| 乌贼娘 | `ika-usume` | 91 |
| 小老师 | `celia-claire` | 35 |
| 熊熊勇闯异世界 | `kuma-bear` | 72 |
| 为美好的世界献上祝福 | `sekai-shukufuku` | 99 |

### 游戏系列

| 名称 | ID | 数量 |
| --- | --- | ---: |
| 原神 | `ys` | 249 |
| 明日方舟 | `mrfz` | 286 |
| 碧蓝航线 | `blhx` | 53 |
| 车万 | `dongf` | 235 |
| 碧蓝档案 | `blda` | 17 |
| 缘之空 | `yzk` | 13 |
| 少女前线 | `snqx` | 14 |
| 崩坏三 | `bh3` | 18 |

### 虚拟主播系列

| 名称 | ID | 数量 |
| --- | --- | ---: |
| 小鲨鱼 | `gawr-gura` | 782 |
| 雪花菈米 | `yukihana` | 45 |
| 夏色祭 | `natsuiro` | 56 |
| 润羽露西娅 | `uruha-rushia` | 114 |
| 花园Serena | `hanazono-serena` | 32 |
| 笹木咲 | `sasaki-saku` | 20 |
| 角卷绵芽 | `tsunomaki-watame` | 29 |
| 常暗永远 | `tokoyami-towa` | 62 |
| 天宫心 | `amamiya-kokoro` | 36 |
| 兔田佩克菈 | `usada-pekora` | 79 |
| 一伊那尔栖 | `ninomae` | 9 |
| 大神澪 | `ookami-mio` | 99 |
| 星川莎拉 | `sara-hoshikawa` | 36 |
| 樱巫女 | `sakura-miko` | 34 |
| 木口EN | `holoen` | 29 |
| 绊爱 | `kizunaai` | 27 |
| 神乐七奈 | `kagura-nana` | 9 |
| 神乐Mea | `kagura-mea` | 37 |
| 白上吹雪 | `fubuki` | 21 |
| 戌神沁音 | `inugami-korone` | 9 |
| 阿夸 | `aqua` | 93 |
| 猫宫日向 | `nekomiya-hinata` | 293 |

### 表情包系列

| 名称 | ID | 数量 |
| --- | --- | ---: |
| 二次元表情包 | `bqb` | 1,922 |
| 甘城猫猫 | `gcmm` | 155 |
| MC酱 | `mc` | 67 |
| kemomimi兽耳酱 | `kemomimi` | 581 |
| 三次元猫猫 | `miao` | 821 |
| 阿夸 | `akqa` | 33 |
| 柴郡猫猫 | `cheshire` | 997 |
| 猫猫虫咖波 | `capoo` | 478 |
| 塞西莉亚（黑白） | `ceciliabqb` | 118 |
| 塞西莉亚 | `cecilia` | 731 |
| 龙图 | `longtu` | 895 |
| 罗翔 | `luox` | 82 |
| 滑稽 | `huaji` | 81 |
| 熊猫头 | `pand` | 111 |
| 同福客栈 | `tfkz` | 102 |
| 原神 | `ysbqb` | 108 |
| 北方酱 | `beifang` | 400 |
| 柯南 | `kenan` | 178 |
| 猫和老鼠 | `tomandjerry` | 141 |
| 小白人 | `whitevillain` | 641 |
| 小婳 | `xiaohua` | 103 |
| 商业头盔 | `toukui` | 144 |
| 牢饭降临到我身边 | `laofan` | 192 |
| 狗妈 | `goum` | 109 |
| 索菲 | `sofei` | 125 |
| 滑稽嘤嘤嘤 | `yingyy` | 99 |
| 草莓果酱 | `caomeiguo` | 26 |
| 和泉纱雾 | `shawu` | 208 |

### 角色系列

| 名称 | ID | 数量 |
| --- | --- | ---: |
| 猫羽雫 | `myn` | 105 |
| 樱岛麻衣 | `ydmy` | 73 |
| 初音未来 | `miku` | 1,200 |
| 洛天依 | `tianyi` | 97 |
| 五更琉璃 | `gokou-ruri` | 250 |
| 椎名真白 | `mashiro` | 172 |
| 鹿乃 | `kano` | 129 |
| Saber | `saber` | 210 |
| 四系乃 | `yoshino` | 96 |
| 见崎鸣 | `misakimei` | 164 |
| 阿卡林 | `akari` | 62 |
| 康娜 | `kanna` | 262 |
| 喵帕斯 | `miaops` | 202 |
| 妮姆芙 | `nymph` | 78 |
| 诺艾尔 | `noel` | 70 |
| 时崎狂三 | `kurumi` | 50 |
| 薇尔莉特 | `violet` | 106 |
| 忍野忍 | `shinobu` | 381 |
| 风见一姬 | `kazuki` | 69 |
| 伊莉雅 | `iliya` | 83 |
| 碧翠丝 | `beatrice` | 268 |
| 土间埋 | `umr` | 183 |
| 雷姆 | `rem` | 137 |
| 阿波连 | `aharen` | 87 |
| 国家队02 | `02` | 79 |
| 阿尼亚 | `aniya` | 57 |
| 高木 | `takagi` | 26 |
| 御坂美琴 | `misaka-mikoto` | 63 |
| 约尔 | `yor` | 91 |
| 水原千鹤 | `mizuhara` | 93 |
| 矢泽妮可 | `nico` | 99 |
| 唐可可 | `tangkk` | 149 |
| 千反田爱瑠 | `eru` | 119 |
| 亚丝娜 | `asuna` | 272 |
| 香风智乃 | `chiro` | 252 |
| 凯露 | `karyl` | 209 |
| 灰原哀 | `haibara` | 123 |
| 雏鹤爱 | `hinatsuru` | 130 |
| 志摩凛 | `shimarin` | 140 |
| 小鸟游六花 | `rikka` | 233 |
| 加藤惠 | `katoumegumi` | 45 |
| 雪之下雪乃 | `yukino` | 193 |
| 谢丝塔 | `siesta` | 107 |
| 早坂爱 | `hayasakaai` | 90 |
| 四宫辉夜 | `kaguya` | 102 |
| 凉宫春日 | `haruhi` | 112 |
| 藤原千花 | `chika` | 38 |
| 祢豆子 | `nezuko` | 81 |
| 小野寺小咲 | `onoderaoosaki` | 39 |
| 中野三玖 | `nakanomiku` | 229 |
| 伊蕾娜 | `elaina` | 62 |
| 佐天泪子 | `ruiko` | 92 |
| 白井黑子 | `kuroko` | 120 |
| 泉此方 | `konata` | 61 |
| 白银圭 | `shiroganekei` | 50 |
| 伊井野弥子 | `linomiko` | 109 |
| 立华奏 | `kanade` | 76 |
| 喜多川海梦 | `kitagawa-marin` | 197 |
| 熊污女[雨宿町] | `amayadori-machi` | 166 |
| 牧濑红莉栖 | `makise-kurisu` | 97 |
| 艾拉 | `lsla` | 39 |
| 蝶祈 | `yuzuriha-inori` | 181 |
| 伊卡洛斯 | `uranus-queen` | 90 |
| 八寻宁宁 | `yashiro-nene` | 62 |
| 菲洛 | `filo` | 190 |
| 食蜂操祈 | `shokuho-isaki` | 290 |
| 我妻由乃 | `gasai-yuno` | 64 |
| 长瀞同学 | `nagatoro-hayase` | 76 |
| 蜘蛛子 | `noname-kumo` | 83 |
| 和泉纱雾 | `izumi-sagiri` | 115 |
| 栗山未来 | `kuriyama-mirai` | 45 |
| 奈亚子 | `nyaruko` | 46 |
| 沙优 | `ogiwara-sayu` | 41 |
| 维包子 | `blois` | 73 |
| 摘希 | `miniwa-tsumiki` | 48 |

## 接入建议

1. 客户端只应依赖 `pic.php` 返回的最终图片 URL，不要解析或依赖预览页 HTML。
2. 图片 URL 可能重定向到站点 CDN；图片加载器必须允许 HTTPS 重定向，并配置内存与磁盘缓存。
3. 服务端没有公开速率限制、稳定性 SLA 或版本策略。生产使用应设置连接/读取超时、失败占位图和退避重试，不要高频轮询。
4. 部分分类名称和图片来源具有年龄分级、版权或内容敏感性风险。接入前应根据产品受众做白名单筛选，并遵守图片作者和接口站点的使用要求。
5. `miyone` 在首页标注为 0 张；不建议作为默认分类。应用默认壁纸建议优先用横屏 `pc`、竖屏 `sjpic` 或通用二次元 `img1`。
