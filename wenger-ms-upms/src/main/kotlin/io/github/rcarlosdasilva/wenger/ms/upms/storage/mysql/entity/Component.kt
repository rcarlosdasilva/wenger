package io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.entity

import io.github.rcarlosdasilva.wenger.ms.arc.SystemEntity
import com.baomidou.mybatisplus.annotations.TableField
import com.baomidou.mybatisplus.annotations.TableName

/**
 * 页面组件表，描述页面中的主要组成部分
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@TableName("upms_component")
class Component : SystemEntity<Component>() {

  @TableField("feature_id")
  var featureId: Long? = null
  @TableField("authority_id")
  var authorityId: Long? = null
  /**
   * 摘要，用于做权限判断
   */
  var digest: String? = null
  var description: String? = null

  companion object {
    const val serialVersionUID = 1L
  }

}