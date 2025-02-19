package kr.co.lotteon.dto.company;

import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class StoryPageRequestDTO {
    @Builder.Default
    private int no = 1;
    @Builder.Default
    private int pg = 1;
    @Builder.Default
    private int size = 9;

    private String cate2;

    public Pageable getPageable(String sort){
        return PageRequest.of(this.pg - 1, this.size, Sort.by(sort).descending());
    }
}
