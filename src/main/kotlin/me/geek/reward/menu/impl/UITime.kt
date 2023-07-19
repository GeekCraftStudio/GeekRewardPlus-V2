package me.geek.reward.menu.impl

import me.geek.reward.api.RewardConfig
import me.geek.reward.api.RewardManager
import me.geek.reward.api.data.ExpIryBuilder
import me.geek.reward.api.data.PlayerData
import me.geek.reward.kether.KetherAPI
import me.geek.reward.menu.Menu
import me.geek.reward.menu.MenuData
import me.geek.reward.menu.MenuIcon
import org.bukkit.Material
import org.bukkit.entity.Player
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.buildItem

/**
 * @作者: 老廖
 * @时间: 2023/7/16 16:14
 * @包: me.geek.reward.menu.impl
 */
fun Player.openTimeUI(data: PlayerData, menuData: MenuData = Menu.timeMenuData) {

    openMenu<Linked<RewardConfig<ExpIryBuilder>>>(menuData.title.replacePlaceholder(this)) {

        map(*menuData.layout)

        rows(menuData.layout.size)

        slots(menuData.itemUISlots)

        elements { RewardManager.timeConfigCache }

        onGenerate { player, e, _, _ ->
            menuData.menuIcon['@']?.let { icon ->
                buildItem(icon.mats) {

                    val state = e.parse(data.timeKey, data.time)

                    val max = e.parseValue()

                    // 解析展示物品名称
                    name = icon.name.replacePlaceholder(player)
                        .replace("{max_value}", max)
                        .replace("{now_value}", data.time.getExpiryFormat())
                        .replace("{state}", state)

                    // 添加配置包描述
                  //  e.info.forEach {
                   //     lore.add(it.replacePlaceholder(player))
                    //}

                    // 添加图标描述
                    icon.lore.forEach {
                        if (it.contains("{info}")) {
                            e.info.forEach { info ->
                                lore.add(info.replacePlaceholder(player))
                            }
                        } else {
                            lore.add(
                                it.replacePlaceholder(player)
                                    .replace("{max_value}", max)
                                    .replace("{now_value}", data.time.getExpiryFormat())
                                    .replace("{state}", state)
                            )
                        }
                    }
                    customModelData = icon.model
                }
            } ?: error("找不到可用的奖励展示图标配置...")
        }


        onClick { _, element ->

            if (data.time.millis >= element.value.millis) {
                if (data.pointsKey.find { it == element.id } != null) {
                    KetherAPI.instantKether(this@openTimeUI, element.require.achieve)
                } else {
                    // 允许领取
                    data.pointsKey.add(element.id)
                    KetherAPI.instantKether(this@openTimeUI, element.require.allow)
                }
            } else KetherAPI.instantKether(this@openTimeUI, element.require.deny)
        }



        // 构建其它图标
        menuData.menuIcon.forEach { (key, icon) ->
            when (key) {
                '<' -> {
                    set(icon.char, buildItem(icon.mats) {
                        name = icon.name.replacePlaceholder(this@openTimeUI)
                        lore.addAll(icon.lore.replacePlaceholder(this@openTimeUI))
                        customModelData = icon.model
                        hideAll()
                    }) {
                        if (icon.action.isNotEmpty()) KetherAPI.eval(this.clicker, icon.action)
                        if (hasPreviousPage()) {
                            page(page-1)
                            openInventory(build())
                        }
                    }
                }
                '>' -> {
                    set(icon.char, buildItem(icon.mats) {
                        name = icon.name.replacePlaceholder(this@openTimeUI)
                        lore.addAll(icon.lore.replacePlaceholder(this@openTimeUI))
                        customModelData = icon.model
                        hideAll()
                    }) {
                        if (icon.action.isNotEmpty()) KetherAPI.eval(this.clicker, icon.action)
                        if (hasPreviousPage()) {
                            page(page+1)
                            openInventory(build())
                        }
                    }
                }
                // 其它任意图标如果有动作则执行
                else -> {
                    if (key != '@')
                    set(key, buildItem(icon.mats) {
                        name = icon.name.replacePlaceholder(this@openTimeUI)
                        lore.addAll(icon.lore.replacePlaceholder(this@openTimeUI))
                        customModelData = icon.model
                    }) {
                        if (icon.action.isNotEmpty()) KetherAPI.eval(this.clicker, icon.action)
                    }
                }
            }
        }
    }
}