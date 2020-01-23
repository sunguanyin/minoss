package io.javac.minoss.minosscommon.model.jwt;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author pencilso
 * @date 2020/1/23 8:07 下午
 */
@Data
@Accessors(chain = true)
public class JwtAuthEntity {

    private Long uMid;

    private String salt;

    private Long loginTime;
}
