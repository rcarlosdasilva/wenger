package io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.entity

import io.github.rcarlosdasilva.wenger.ms.arc.BusinessEntity
import java.util.Date
import com.baomidou.mybatisplus.annotations.TableField
import com.baomidou.mybatisplus.annotations.TableName

/**
 * 系统账号（包含超管、客户、用户）
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@TableName("upms_account")
class Account : BusinessEntity<Account>() {

  /**
   * 账号通过联系人表关联到机构
   */
  @TableField("contact_id")
  var contactId: Long? = null
  /**
   * 当前处于的角色（用户归属多个分组时使用）
   */
  @TableField("role_id")
  var roleId: Long? = null
  /**
   * 用户的唯一资源标识码
   */
  var code: String? = null
  /**
   * 账号类型，取值：ADM 超管用户，CLT 客户，USR 普通用户
   */
  var type: String? = null
  /**
   * 账号
   */
  var username: String? = null
  /**
   * 密码（BCrypt）
   */
  var password: String? = null
  /**
   * Remember Me功能，序列标识
   */
  @TableField("signet_series")
  var signetSeries: String? = null
  /**
   * Remember Me功能，自动认证token
   */
  @TableField("signet_token")
  var signetToken: String? = null
  /**
   * Remember Me时间
   */
  @TableField("time_of_scar")
  var timeOfScar: Date? = null
  /**
   * 昵称
   */
  var nickname: String? = null
  /**
   * 头像
   */
  var avatar: String? = null
  /**
   * 最后一次登录时间
   */
  @TableField("time_of_lastlogin")
  var timeOfLastlogin: Date? = null
  /**
   * 登录次数
   */
  @TableField("count_of_login")
  var countOfLogin: Integer? = null
  /**
   * 是否在线
   */
  @TableField("flag_online")
  var online: Boolean? = null
  /**
   * 账号是否异常被锁定，默认否，一般在有非法行为或欠费之类的情况下导致账号异常，不可用
   */
  @TableField("flag_abnormal")
  var abnormal: Boolean? = null
  /**
   * 用户账号是否已过期，默认否
   */
  @TableField("flag_expired")
  var expired: Boolean? = null

  companion object {
    const val serialVersionUID = 1L
  }

}