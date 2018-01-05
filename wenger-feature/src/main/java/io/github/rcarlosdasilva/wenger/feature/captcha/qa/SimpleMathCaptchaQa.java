package io.github.rcarlosdasilva.wenger.feature.captcha.qa;

import io.github.rcarlosdasilva.kits.string.TextHelper;

import java.util.Random;

/**
 * 简单运算（加减乘除）验证码
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public class SimpleMathCaptchaQa extends AbstractCaptchaQa {

  private static final String OPERATORS = "+-*/";
  private static final String EQUAL = "=";
  private static final Random RANDOM = new Random();

  /**
   * @param seed 运算符，取值（+ - * /），超出取值范围，运算符随机
   */
  @Override
  public void make(Object seed) {
    if (seed == null) {
      seed = "";
    }

    String operator = String.valueOf(seed);
    if (operator.length() != 1 || !OPERATORS.contains(operator)) {
      operator = TextHelper.random(1, OPERATORS);
    }

    int v1 = 0;
    int v2 = 0;
    int v3 = 0;
    switch (operator) {
      case "+": {
        v1 = RANDOM.nextInt(20);
        v2 = RANDOM.nextInt(20);
        v3 = v1 + v2;
        break;
      }
      case "-": {
        v1 = RANDOM.nextInt(20);
        v2 = RANDOM.nextInt(20);
        if (v2 > v1) {
          v1 = v1 + v2;
          v2 = v1 - v2;
          v1 = v1 - v2;
        }
        v3 = v1 - v2;
        break;
      }
      case "*": {
        v1 = RANDOM.nextInt(10);
        v2 = RANDOM.nextInt(10);
        v3 = v1 * v2;
        break;
      }
      case "/": {
        v1 = RANDOM.nextInt(10);
        v2 = RANDOM.nextInt(10);
        v1 *= v2;
        v3 = v1 / v2;
        break;
      }
      default:
    }

    setQuestion(TextHelper.concat(String.valueOf(v1), operator, String.valueOf(v2), EQUAL));
    setAnswer(String.valueOf(v3));
  }

}
