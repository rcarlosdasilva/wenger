package io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.entity

import io.github.rcarlosdasilva.wenger.ms.arc.SystemEntity
import com.baomidou.mybatisplus.annotations.TableField
import com.baomidou.mybatisplus.annotations.TableName

/**
 * 菜单表
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@TableName("upms_menu")
class Menu : SystemEntity<Menu>() {

  @TableField("parent_id")
  var parentId: Long? = null
  @TableField("authority_id")
  var authorityId: Long? = null
  /**
   * 摘要，用于做权限判断
   */
  var digest: String? = null
  var name: String? = null
  /**
   * 排序，从0开始，0最往前
   */
  var sort: Integer? = null
  var icon: String? = null
  var url: String? = null
  var description: String? = null

  companion object {
    const val serialVersionUID = 1L
  }

}