package kr.co.lotteon.repository.custom;

import com.querydsl.core.Tuple;
import kr.co.lotteon.dto.admin.AdminMemberPageRequestDTO;
import kr.co.lotteon.dto.admin.AdminProductPageRequestDTO;
import kr.co.lotteon.entity.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {

    // 월별 가입 count 조회
    public List<Tuple> selectMemberForChart();

    // 회원 목록 기본 조회
    public Page<Member> selectMemberList(AdminMemberPageRequestDTO adminMemberPageRequestDTO, Pageable pageable);

    // 회원 목록 검색 조회
    public Page<Member> searchMemberList(AdminMemberPageRequestDTO adminMemberPageRequestDTO, Pageable pageable);

}
