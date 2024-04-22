package kr.co.lotteon.dto.product;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class WishDTO {
    private int wishNo;
    private String uid;
    private int prodNo;
}
