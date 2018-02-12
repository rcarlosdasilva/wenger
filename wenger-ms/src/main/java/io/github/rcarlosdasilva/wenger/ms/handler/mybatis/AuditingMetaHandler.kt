package io.github.rcarlosdasilva.wenger.ms.handler.mybatis

import com.baomidou.mybatisplus.mapper.MetaObjectHandler
import io.github.rcarlosdasilva.wenger.feature.sequence.SequenceHandler
import io.github.rcarlosdasilva.wenger.ms.handler.cloud.PrincipalHandler
import org.apache.ibatis.reflection.MetaObject
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component

/**
 * 依赖于MyBatis Plus框架的审计字段自动填充
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConditionalOnBean(value = [SequenceHandler::class, PrincipalHandler::class])
@Component
class AuditingHandler @Autowired constructor(
  private val sequenceHandler: SequenceHandler,
  private val principalHandler: PrincipalHandler
) : MetaObjectHandler() {

  override fun insertFill(metaObject: MetaObject) {
    setFieldValByName(FIELD_NAME_ID, sequenceHandler.id(), metaObject)
    setFieldValByName(FIELD_NAME_CREATE_AT, DateTime.now().toDate(), metaObject)
    setFieldValByName(FIELD_NAME_UPDATE_AT, DateTime.now().toDate(), metaObject)
    setFieldValByName(FIELD_NAME_CREATE_BY, principalHandler.get(), metaObject)
    setFieldValByName(FIELD_NAME_UPDATE_BY, principalHandler.get(), metaObject)
  }

  override fun updateFill(metaObject: MetaObject) {
    setFieldValByName(FIELD_NAME_UPDATE_AT, DateTime.now().toDate(), metaObject)
    setFieldValByName(FIELD_NAME_UPDATE_BY, principalHandler.get(), metaObject)
  }

  companion object {
    private const val FIELD_NAME_ID = "id"
    private const val FIELD_NAME_CREATE_AT = "createAt"
    private const val FIELD_NAME_UPDATE_AT = "updateAt"
    private const val FIELD_NAME_CREATE_BY = "createBy"
    private const val FIELD_NAME_UPDATE_BY = "updateBy"
  }

}