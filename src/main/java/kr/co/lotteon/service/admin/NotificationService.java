package kr.co.lotteon.service.admin;

import kr.co.lotteon.component.SseEmitters;
import kr.co.lotteon.entity.member.Member;
import kr.co.lotteon.repository.cs.BoardRepository;
import kr.co.lotteon.repository.product.ProductRepository;
import kr.co.lotteon.security.MyUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final SseEmitters sseEmitters;
    private final BoardRepository boardRepository;
    private final ProductRepository productRepository;

    // 로그인 시 최초 연결 요청
    public ResponseEntity<SseEmitter> subscribe() {

        log.info("SSE Service 1 ");
        Member member = whoAmI();

        // 현재 사용자의 SseEmitter가 이미 존재하는지 확인 - confirm창 최초 한번만
        SseEmitter existingEmitter = SseEmitters.sseEmitters.get(member.getUid());
        if (existingEmitter != null) {
            log.info("이미 연결된 사용자입니다.");
            try {
                existingEmitter.send(SseEmitter.event()
                        .name("connect")
                        .data("none"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok().body(existingEmitter);
        }else {

            // 연결 요청한 클라이언트의 SseEmitter 객체 생성 후 저장
            SseEmitter sseEmitter = new SseEmitter(300_000L); // 만료 시간 5분
            sseEmitters.add(member.getUid(), sseEmitter);

            // 해당 판매자의 상품번호 전부 조회
            List<Integer> prodNos = productRepository.selectProdNoForQna(member.getUid());
            log.info("상품번호 전부 조회 " + prodNos.toString());

            int count = boardRepository.countSellerQna(prodNos);

            log.info("SSE Service sseEmitters " + sseEmitters);
            try {
            /*
                Emitter를 생성하고 나서 만료 시간까지 아무런 데이터도 보내지 않으면 재연결 요청시 503 Service Unavailable 에러가 발생
                -> 503 에러 방지를 위한 더미 데이터 전송
            */
                if (count >= 1) {
                    log.info("서비스 1 : " + count);
                    sseEmitter.send(SseEmitter.event()
                            .name("connect") // 해당 이벤트의 이름 지정 : View(Client)에서 해당 이름으로 이벤트를 받음
                            .data(member.getNick() + "님의 답변 대기 중인 문의가 " + count + "건 있습니다."));
                } else {
                    log.info("서비스 2 : " + count);
                    sseEmitter.send(SseEmitter.event()
                            .name("connect")
                            .data("none"));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            log.info("SSE Service 2 ");

            // SseEmitter::complete : 연결 종료
            sseEmitter.onTimeout(sseEmitter::complete);
            sseEmitter.onError((e) -> sseEmitter.complete());

            log.info("SSE Service 3 ");
            return ResponseEntity.ok().body(sseEmitter);
        }
    }
    // 문의글 작성되면 해당 판매자에게 알람
    private void send(String sellerId) {

        SseEmitter sseEmitter = SseEmitters.sseEmitters.get(sellerId);
        try {
            log.info("send to 판매자 : " + sellerId);
            // 이벤트 데이터 전송
            sseEmitter.send(SseEmitter.event()
                            .name("send")
                    .data("새로 작성된 상품 문의가 있습니다."));

        } catch (IOException | IllegalStateException e) {
            log.error("IOException | IllegalStateException is occurred. ", e);
        }
    }

    // 사용자 정보 함수
    public Member whoAmI(){
        // 현재 로그인 중인 사용자 정보 불러오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 로그인 중일 때 해당 사용자 id를 seller에 입력
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        Member seller= userDetails.getMember();

        return seller;
    }
}
