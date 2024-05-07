package kr.co.lotteon.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.lotteon.dto.admin.BannerDTO;
import kr.co.lotteon.dto.cs.BoardDTO;
import kr.co.lotteon.dto.cs.CsPageRequestDTO;

import kr.co.lotteon.dto.cs.CsPageResponseDTO;
import kr.co.lotteon.dto.member.CouponDTO;
import kr.co.lotteon.dto.member.MemberDTO;
import kr.co.lotteon.dto.member.MyInfoDTO;
import kr.co.lotteon.dto.member.point.PointDTO;
import kr.co.lotteon.dto.product.*;
import kr.co.lotteon.dto.member.point.PointPageRequestDTO;
import kr.co.lotteon.dto.member.point.PointPageResponseDTO;

import kr.co.lotteon.entity.member.Member;
import kr.co.lotteon.entity.member.Point;
import kr.co.lotteon.security.MyUserDetails;
import kr.co.lotteon.service.admin.BannerService;
import kr.co.lotteon.service.member.MemberService;
import kr.co.lotteon.service.my.MyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MyController {

    private final BannerService bannerService;
    private final MemberService memberService;
    private final MyService myService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;


    // my - home (마이페이지 메인) 페이지 매핑
    @GetMapping("/my/home")
    public String home(Model model, @RequestParam String uid){
        log.info("uid : "+uid);

        // 마이페이지 배너
        List<BannerDTO> myPageBanners = bannerService.selectBanners("myPage");

        model.addAttribute("myPageBanners",myPageBanners);
        return "/my/home";
    }

    // my - info (나의 설정) 페이지 매핑
    @GetMapping("/my/info")
    public String info(Model model,@RequestParam String uid){

        MemberDTO memberDTO =memberService.findByUid(uid);
        model.addAttribute("memberDTO",memberDTO);

        return "/my/info";
    }
    @PostMapping("/my/info")
    public String info(MemberDTO changeMemberDTO,@RequestParam String uid){
        log.info("PASSWORD "+changeMemberDTO.getPass());

        log.info("changeMemberDTO"+changeMemberDTO);
        MemberDTO memberDTO = memberService.findByUid(uid);
        memberDTO.setPass(changeMemberDTO.getPass());
        memberDTO.setNick(changeMemberDTO.getNick());
        memberDTO.setEmail(changeMemberDTO.getEmail());
        memberDTO.setHp(changeMemberDTO.getHp());
        memberDTO.setZip(changeMemberDTO.getZip());
        memberDTO.setAddr1(changeMemberDTO.getAddr1());
        memberDTO.setAddr2(changeMemberDTO.getAddr2());

        memberService.save(memberDTO);

        return "redirect:/index?success=200";
    }
    @GetMapping("/my/infoAccessCheck")
    public String infoAccessCheck() {
        return "/my/infoAccessCheck";
    }

    @PostMapping("/my/infoAccessCheck")
    @ResponseBody
    public boolean infoAccessCheck(@RequestBody Map<String, String> request ) {

        log.info("나의 설정 들어가기");
        String uid = request.get("uid");
        log.info("아이디 : "+request.get("uid"));

        String pass = request.get("pass");
        log.info("입력한 비번 : "+request.get("pass"));


        MemberDTO memberDTO = memberService.findByUid(uid);
        log.info("DB에 저장된 비번 : "+memberDTO.getPass());

        boolean result = passwordEncoder.matches(pass, memberDTO.getPass());


        log.info("아이디비번 일치 : "+result);

        return result;
    }

    // 추가
    @ResponseBody
    @GetMapping("/my/formMyinfoPassChange")
    public String formMyinfoPassChange(@RequestBody Map<String, String> request ) {
        log.info("비밀번호 수정 들어가기");

        String uid = request.get("uid");
        log.info("입력된 아이디 : "+uid);

        String pass = request.get("pass");
        log.info("수정한 비번 : "+pass);

        memberService.updatePass(uid,pass);



        return "success";
    }

    // 닉네임수정
    @ResponseBody
    @PostMapping("/my/formMyinfoNickChange")
    public String formMyinfoNickChange(@RequestBody Map<String, String> request ) {
        log.info("닉네임 수정 들어가기");

        String uid = request.get("uid");
        log.info("입력된 아이디 : "+uid);

        String nick = request.get("nick");
        log.info("수정한 닉네임 : "+nick);

        Optional<Member> member = memberService.selectMemberByUidAndNickname(uid, nick);
        log.info(member.toString());
        if(member.isPresent()){
            return "fail";

        }else{

            memberService.updateNick(uid,nick);

            return "success";

        }

    }



//    @ResponseBody
//    @PostMapping("/my/withdraw")
//    public String withdraw(@RequestParam String uid, String inputPass) {
//        Authentication authentication = new UsernamePasswordAuthenticationToken(uid, inputPass);
//        Authentication result = authenticationManager.authenticate(authentication);
//
//        if (result.isAuthenticated()) {
//            memberService.updateWdate(uid);
//            return "success";
//        } else {
//            return "fail";
//        }
//    }
//    @ResponseBody
//    @PostMapping("/my/withdrawFinal")
//    public String withdrawFinal(@RequestBody MemberDTO memberDTO) {
//        log.info("=========회원정보수정========== : "+memberDTO);
//        memberService.save(memberDTO);
//        return "success";
//    }



    //추가 끝


    // my - order (나의 전체 주문내역) 페이지 매핑
    @GetMapping("/my/order")
    public String order(){
        return "/my/order";
    }

    @GetMapping("/my/point")
    public String point(@RequestParam String uid, Model model, PointPageRequestDTO pointPageRequestDTO) {
        PointPageResponseDTO pointPageResponseDTO = myService.getPointListByUid(uid, pointPageRequestDTO);
        model.addAttribute("pointPageResponseDTO", pointPageResponseDTO);
        return "/my/point";
    }

    // my - point (나의 포인트) 페이지 매핑
    @GetMapping("/my/pointList")
    @ResponseBody
    public PointPageResponseDTO pointList(PointPageRequestDTO pointPageRequestDTO, @AuthenticationPrincipal Object principal) {
        log.info("pointPageRequestDTO................ : " + pointPageRequestDTO);

        Member member = ((MyUserDetails) principal).getMember();
        String uid = member.getUid();
        return myService.findPointList(uid, pointPageRequestDTO);
    }

    // my - coupon 페이지 매핑
    @GetMapping("/my/coupon")
    public String coupon(Model model,@RequestParam String uid){

        List<CouponDTO> coupons = myService.findCouponsByUid(uid);

        int count = myService.findCouponCountByUidAndUseYn(uid);

        model.addAttribute("coupons",coupons);
        model.addAttribute("count",count);

        log.info("내 쿠폰"+coupons);
        log.info("count"+count);


        return "/my/coupon";
    }

    @GetMapping("/my/myInfo")
    @ResponseBody
    public MyInfoDTO myInfo(@AuthenticationPrincipal Object principal) {
        Member member = ((MyUserDetails) principal).getMember();
        String uid = member.getUid();

        log.info("uid " + uid);

        int couponCount = myService.findCouponCountByUidAndUseYn(uid);
        log.info("쿠폰의 수"+couponCount);

        List<String> ordStatusList = Arrays.asList("배송준비", "배송중");
        int orderCount = myService.countOrderItemsByUidAndOrdStatusIn(uid, ordStatusList);
        log.info("주문의 수"+orderCount);

        List<String> statusList = Arrays.asList("검토중", "답변완료");
        int qnaCount = myService.countByUidAndStatusIn(uid,statusList);
        log.info("문의의 수"+qnaCount);

        log.info("포인트 값"+member.getPoint());

        return MyInfoDTO.builder()
                .myPoint(member.getPoint())
                .couponCount(couponCount)
                .orderCount(orderCount)
                .qnaCount(qnaCount)
                .build();
    }




    // my - qna (마이페이지 문의하기) 페이지 매핑
    @GetMapping("/my/qna")
    public String qna(Model model, @RequestParam String uid, CsPageRequestDTO csPageRequestDTO){

        // 문의 목록 조회
        CsPageResponseDTO csPageResponseDTO = null;
        csPageResponseDTO = myService.QnaList(uid,csPageRequestDTO);



        model.addAttribute("csPageResponseDTO", csPageResponseDTO);

        log.info("문의 목록 조회"+csPageResponseDTO);



        return "/my/qna";
    }
    // my - review (나의 리뷰내역) 페이지 매핑
    @GetMapping("/my/review")
    public String review(Model model, @RequestParam String uid, ProductReviewPageRequestDTO productReviewPageRequestDTO){
        ProductReviewPageResponseDTO productReviewPageResponseDTO = null;
        productReviewPageResponseDTO = myService.reviewList(uid,productReviewPageRequestDTO);

        model.addAttribute("productReviewPageResponseDTO",productReviewPageResponseDTO);

        return "/my/review";
    }
}
