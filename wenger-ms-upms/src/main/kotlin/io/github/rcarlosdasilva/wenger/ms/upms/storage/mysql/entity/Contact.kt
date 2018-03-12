package io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.entity

import io.github.rcarlosdasilva.wenger.ms.arc.BusinessEntity
import com.baomidou.mybatisplus.annotations.TableField
import com.baomidou.mybatisplus.annotations.TableName

/**
 * 联系人信息表
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@TableName("upms_contact")
class Contact : BusinessEntity<Contact>() {

  /**
   * 姓名
   */
  var name: String? = null
  /**
   * 英文名（可兼容非中文名）
   */
  @TableField("name_en")
  var nameEn: String? = null
  /**
   * 称谓，取值：NON 无， MRG 先生， MSG 女士， MIS 小姐
   */
  var title: String? = null
  /**
   * 手机
   */
  var mobile: String? = null
  /**
   * 座机
   */
  var telephone: String? = null
  /**
   * 邮件地址
   */
  var mail: String? = null
  /**
   * 联系地址
   */
  var address: String? = null

  companion object {
    const val serialVersionUID = 1L
  }

}