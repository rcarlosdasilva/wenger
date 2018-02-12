package io.github.rcarlosdasilva.wenger.feature.aliyun.samples;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.client.CloudTopic;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.common.utils.ServiceSettings;
import com.aliyun.mns.model.Base64TopicMessage;
import com.aliyun.mns.model.Message;
import com.aliyun.mns.model.TopicMessage;
import org.junit.Test;

public class MnsSample {

  @Test
  public void queue() {
    CloudAccount account = new CloudAccount(
        ServiceSettings.getMNSAccessKeyId(),
        ServiceSettings.getMNSAccessKeySecret(),
        ServiceSettings.getMNSAccountEndpoint());
    MNSClient client = account.getMNSClient();

    CloudQueue queue = client.getQueueRef("queue-test");
//    Message message = new Message();
//    message.setMessageBody("message_body_abc");
//    Message putMsg = queue.putMessage(message);
//    System.out.println("PutMessage has MsgId: " + putMsg.getMessageId());
    Message popMsg = queue.popMessage();
    System.out.println("PopMessage Body: "
        + popMsg.getMessageBodyAsString());

//    queue.deleteMessage(popMsg.getReceiptHandle());
  }

  @Test
  public void topic() {
    CloudAccount account = new CloudAccount(
        ServiceSettings.getMNSAccessKeyId(),
        ServiceSettings.getMNSAccessKeySecret(),
        ServiceSettings.getMNSAccountEndpoint());
    MNSClient client = account.getMNSClient();

    CloudTopic topic = client.getTopicRef("topic-test");

    TopicMessage msg = new Base64TopicMessage();
    msg.setMessageBody("hello world! haha, GO! Queue!" + System.currentTimeMillis());
    msg = topic.publishMessage(msg);
    System.out.println(msg.getMessageId());
    System.out.println(msg.getMessageBodyMD5());
  }

}
