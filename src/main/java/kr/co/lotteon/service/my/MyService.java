package kr.co.lotteon.service.my;

import kr.co.lotteon.dto.member.CouponDTO;
import kr.co.lotteon.entity.member.Coupon;
import kr.co.lotteon.repository.cs.BoardRepository;
import kr.co.lotteon.repository.my.CouponRepository;
import kr.co.lotteon.repository.product.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyService {

    private final CouponRepository couponRepository;
    private final ModelMapper modelMapper;
    private final OrderItemRepository orderItemRepository;
    private final BoardRepository boardRepository;

    public List<CouponDTO> findCouponsByUid(String uid){
        log.info("내 쿠폰"+couponRepository.findCouponsByUid(uid));
        List<Coupon> result=couponRepository.findCouponsByUid(uid);
        List<CouponDTO> couponDTOS=result.stream().map(coupons->modelMapper.map(coupons,CouponDTO.class))
                .collect(Collectors.toList());
        for (CouponDTO couponDTO : couponDTOS) {
            couponDTO.changeUseYnString();
        }
        return couponDTOS;
    }

    public int findCouponCountByUidAndUseYn(String uid) {
        return couponRepository.countByUidAndUseYn(uid, "Y");
    }

    public int findOrderByUidAndOrdStatus(String uid) {
        return orderItemRepository.countByUidAndOrdStatusNot(uid,"배송중");
    }

    public int findQnaByUidAndStatus(String uid) {
        return boardRepository.countByUidAndStatusNot(uid, "답변완료");
    }



}
