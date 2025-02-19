package kr.co.lotteon.dto.admin;

import kr.co.lotteon.dto.cs.BoardDTO;
import kr.co.lotteon.dto.product.ProductDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AdminBoardPageResponseDTO {
    private List<BoardDTO> dtoList;
    private int pg;
    private int size;
    private int total;
    private int startNo;
    private String group;
    private String type;
    private String keyword;

    private int start, end;
    private boolean prev, next;

    @Builder
    public AdminBoardPageResponseDTO(AdminBoardPageRequestDTO adminBoardPageRequestDTO, List<BoardDTO> dtoList, int total){

        this.pg = adminBoardPageRequestDTO.getPg();
        this.size = adminBoardPageRequestDTO.getSize();
        this.total = total;
        this.dtoList = dtoList;
        this.group = adminBoardPageRequestDTO.getGroup();
        this.type = adminBoardPageRequestDTO.getType();
        this.keyword = adminBoardPageRequestDTO.getKeyword();

        this.startNo = total - ((pg - 1) * size);
        this.end = (int) (Math.ceil(this.pg/10.0))*10;
        this.start = this.end - 9;

        int last = (int) (Math.ceil(total / (double) size));
        this.end = end > last ? last : end;
        this.prev = this.start > 1;
        this.next = total > this.end * this.size;
    }
}
