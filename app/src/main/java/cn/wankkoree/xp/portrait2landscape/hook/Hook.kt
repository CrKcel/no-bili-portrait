package cn.wankkoree.xp.portrait2landscape.hook

import android.net.Uri
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.MembersType
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.type.java.StringClass

class Hook: YukiBaseHooker() {
    override fun onHook() {
        // Legacy: rewrite BasicIndexItem.uri (used by 7.x routing). Kept as 7.x fallback; 8.x ignores it.
        "com.bilibili.pegasus.api.model.BasicIndexItem".hook {
            injectMember {
                allMembers(MembersType.CONSTRUCTOR)
                afterHook {
                    val uri = field {
                        name = "uri"
                        type = StringClass
                    }.get(instance)
                    uri.string().let {
                        if (it.isNotEmpty()) {
//                                loggerD(msg = "BasicIndexItem.<init>(url = $it)")
                            if (it.startsWith("bilibili://story/")) {
                                uri.set("bilibili://video/" + it.substringAfter("bilibili://story/"))
                                loggerD(msg = "初始化视频信息时，竖屏转横屏成功")
                            }
                        }
                    }
                }
            }
            injectMember {
                method {
                    name = "setUri"
                    param(StringClass)
                }
                beforeHook {
                    (args[0]!! as String).let {
                        if (it.isNotEmpty()) {
//                                loggerD(msg = "BasicIndexItem.setUri(url = $it)")
                            if (it.startsWith("bilibili://story/")) {
                                args(0).set("bilibili://video/" + it.substringAfter("bilibili://story/"))
                                loggerD(msg = "设定视频链接时，竖屏转横屏成功")
                            }
                        }
                    }
                }
            }
        }

        // 8.x builds the story URL from avid via RouteRequest.Builder -> BLRouter (bypasses uri above).
        // Hook the Builder ctors (String/Uri) to rewrite story->video; ctor signatures stay unobfuscated across versions.
        "com.bilibili.lib.blrouter.RouteRequest\$Builder".hook {
            // new RouteRequest.Builder("bilibili://story/<avid>")
            injectMember {
                constructor { param(StringClass) }
                beforeHook {
                    (args[0] as? String)?.let {
                        if (it.startsWith("bilibili://story/")) {
                            args(0).set("bilibili://video/" + it.substringAfter("bilibili://story/"))
                            loggerD(msg = "路由构建(String)时，竖屏转横屏成功：$it")
                        }
                    }
                }
            }
            // new RouteRequest.Builder(Uri.parse("bilibili://story/<avid>"))
            injectMember {
                constructor { param(Uri::class.java) }
                beforeHook {
                    (args[0] as? Uri)?.toString()?.let {
                        if (it.startsWith("bilibili://story/")) {
                            args(0).set(Uri.parse("bilibili://video/" + it.substringAfter("bilibili://story/")))
                            loggerD(msg = "路由构建(Uri)时，竖屏转横屏成功：$it")
                        }
                    }
                }
            }
        }
    }
}
