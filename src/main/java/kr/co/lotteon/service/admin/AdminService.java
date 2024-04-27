package kr.co.lotteon.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.Tuple;
import kr.co.lotteon.dto.admin.*;
import kr.co.lotteon.dto.cs.*;
import kr.co.lotteon.dto.member.MemberDTO;
import kr.co.lotteon.dto.product.*;
import kr.co.lotteon.entity.admin.Banner;
import kr.co.lotteon.entity.cs.BoardCateEntity;
import kr.co.lotteon.entity.cs.BoardEntity;
import kr.co.lotteon.entity.cs.BoardTypeEntity;
import kr.co.lotteon.entity.member.Member;
import kr.co.lotteon.entity.member.Terms;
import kr.co.lotteon.entity.product.Cate1;
import kr.co.lotteon.entity.product.Option;
import kr.co.lotteon.entity.product.Product;
import kr.co.lotteon.repository.BannerRepository;
import kr.co.lotteon.repository.cs.BoardCateRepository;
import kr.co.lotteon.repository.cs.BoardFileRepository;
import kr.co.lotteon.repository.cs.BoardRepository;
import kr.co.lotteon.repository.cs.BoardTypeRepository;
import kr.co.lotteon.repository.member.MemberRepository;
import kr.co.lotteon.repository.member.TermsRepository;
import kr.co.lotteon.repository.product.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.beans.Transient;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminService {

    private final BoardRepository boardRepository;
    private final BoardCateRepository boardCateRepository;
    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;
    private final Cate1Repository cate1Repository;
    private final Cate2Repository cate2Repository;
    private final Cate3Repository cate3Repository;
    private final TermsRepository termsRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final BoardTypeRepository typeRepository;
    private final BannerRepository bannerRepository;
    private final BoardFileRepository fileRepository;

    private final ModelMapper modelMapper;


    @Value("${img.upload.path}")
    private String imgUploadPath;


    // 관리자 환경설정 기본환경 정보 - 약관 조회
    public Terms findByTerms() {
        return termsRepository.findById(1).get();
    }

    // 관리자 인덱스 주문 차트 조회
    public List<Map<String, Object>> selectOrderForChart() {
        log.info("월별 주문 count 조회 Serv 1");
        List<Tuple> tuples = orderRepository.selectOrderForChart();
        log.info("월별 주문 count 조회 Serv 2: " + tuples);

        // 총 주문 수를 저장할 변수
        AtomicLong totalOrders = new AtomicLong(0);

        List<Map<String, Object>> jsonResult = tuples.stream()
                .map(tuple -> {
                    int year = tuple.get(0, Integer.class);
                    int month = tuple.get(1, Integer.class);
                    long count = tuple.get(2, long.class);

                    // 총 주문 수에 현재 월의 주문 수를 더함
                    totalOrders.addAndGet(count);

                    Map<String, Object> map = new HashMap<>();
                    map.put("month", month + "월");
                    map.put("count", count);
                    return map;
                })
                .collect(Collectors.toList());

        // 총 주문 수를 결과에 추가
        Map<String, Object> totalMap = new HashMap<>();
        totalMap.put("total", totalOrders.get());
        jsonResult.add(totalMap);

        log.info("월별 주문 count 조회 Serv 3: " + jsonResult);
        return jsonResult;
    }

    // 관리자 인덱스 회원 가입 차트 조회
    public List<Map<String, Object>> selectMemberForChart() {
        log.info("월별 가입자 count 조회 Serv 1");
        List<Tuple> tuples = memberRepository.selectMemberForChart();
        log.info("월별 가입자 count 조회 Serv 2: " + tuples);

        // 총 가입 수를 저장할 변수
        AtomicLong totalMembers = new AtomicLong(0);

        // 조회 결과 List로 반환
        List<Map<String, Object>> jsonResult = tuples.stream()
                .map(tuple -> {
                    int year = tuple.get(0, Integer.class);
                    int month = tuple.get(1, Integer.class);
                    long count = tuple.get(2, long.class);

                    // 총 주문 수에 현재 월의 가입 수를 더함
                    totalMembers.addAndGet(count);

                    Map<String, Object> map = new HashMap<>();
                    map.put("month", month + "월");
                    map.put("count", count);
                    return map;
                })
                .collect(Collectors.toList());

        // 총 가입 수를 결과에 추가
        Map<String, Object> totalMap = new HashMap<>();
        totalMap.put("total", totalMembers.get());
        jsonResult.add(totalMap);

        log.info("월별 가입자 count 조회 Serv 3: " + jsonResult);
        return jsonResult;
    }

    // 관리자 인덱스 공지사항 조회
    public List<BoardDTO> adminSelectNotices() {
        List<Tuple> results = boardRepository.adminSelectBoards("notice");
        List<BoardDTO> dtoList = new ArrayList<>();
        results.forEach(tuple -> {
            // Tuple -> Entity
            BoardEntity board = tuple.get(0, BoardEntity.class);
            String typeName = tuple.get(1, String.class);
            // Entity -> DTO
            BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);
            boardDTO.setTypeName(typeName);
            dtoList.add(boardDTO);
        });

        return dtoList;
    }

    // 관리자 인덱스 고객문의 조회
    public List<BoardDTO> adminSelectQnas() {
        List<Tuple> results = boardRepository.adminSelectBoards("qna");
        List<BoardDTO> dtoList = new ArrayList<>();
        results.forEach(tuple -> {
            // Tuple -> Entity
            BoardEntity board = tuple.get(0, BoardEntity.class);
            String typeName = tuple.get(1, String.class);
            // Entity -> DTO
            BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);
            boardDTO.setTypeName(typeName);
            dtoList.add(boardDTO);
        });

        return dtoList;
    }

    // 관리자 배너 목록
    public List<BannerDTO> bannerList(String cate) {

        // 조회된 Entity List -> DTO List
        return bannerRepository.findByCate(cate).stream()
                .map(banner ->
                        modelMapper.map(banner, BannerDTO.class))
                .collect(Collectors.toList());
    }

    // 관리자 배너 등록
    public void bannerRegister(MultipartFile imgFile, BannerDTO bannerDTO) {

        // 이미지 파일 등록 : 해당 디렉토리 없을 경우 자동 생성
        File file = new File(imgUploadPath);
        if (!file.exists()) {
            file.mkdir();
        }
        String path = file.getAbsolutePath();

        // 원본 파일 폴더 자동 생성
        String orgPath = path + "/orgImage";
        File orgFile = new File(orgPath);
        if (!orgFile.exists()) {
            orgFile.mkdir();
        }
        // 이미지 리사이징
        if (!imgFile.isEmpty()) {

            // oName, sName 구하기
            String oFile = imgFile.getOriginalFilename();
            String ext = oFile.substring(oFile.lastIndexOf("."));
            String sFile = UUID.randomUUID().toString() + ext;

            try {
                // 원본 파일 저장
                imgFile.transferTo(new File(orgFile, sFile));

                // 파일 이름 DTO에 저장
                bannerDTO.setThumb(sFile);

                // cate에 맞는 사이즈로 바꾸기
                switch (bannerDTO.getCate()) {
                    case "main-top":
                        // 리사이징 1200 * 80
                        Thumbnails.of(new File(orgPath, sFile)) // 원본 파일 (경로, 이름)
                                .size(1200, 80) // 원하는 사이즈
                                .toFile(new File(path, sFile)); // 리사이징 파일 (경로, 이름)
                        break;
                    case "main-slider":
                        // 리사이징 985 * 447
                        Thumbnails.of(new File(orgPath, sFile)) // 원본 파일 (경로, 이름)
                                .size(985, 447) // 원하는 사이즈
                                .toFile(new File(path, sFile)); // 리사이징 파일 (경로, 이름)
                        break;
                    case "product":
                        // 리사이징 456 * 60
                        Thumbnails.of(new File(orgPath, sFile)) // 원본 파일 (경로, 이름)
                                .size(456, 60) // 원하는 사이즈
                                .toFile(new File(path, sFile)); // 리사이징 파일 (경로, 이름)
                        break;
                    case "login":
                        // 리사이징 425 * 260
                        Thumbnails.of(new File(orgPath, sFile)) // 원본 파일 (경로, 이름)
                                .size(425, 260) // 원하는 사이즈
                                .toFile(new File(path, sFile)); // 리사이징 파일 (경로, 이름)
                        break;
                    case "myPage":
                        // 리사이징 810 * 86
                        Thumbnails.of(new File(orgPath, sFile)) // 원본 파일 (경로, 이름)
                                .size(810, 86) // 원하는 사이즈
                                .toFile(new File(path, sFile)); // 리사이징 파일 (경로, 이름)
                        break;
                    default:
                        break;
                }

                log.info("리사이징 끝");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // banner Table에 저장
        bannerRepository.save(modelMapper.map(bannerDTO, Banner.class));
    }

    // 관리자 배너 활성화 관리
    public ResponseEntity<?> bannerActChange(int bno) {
        Banner banner = bannerRepository.findById(bno).get();

        // 활성화 -> 비활성화
        if (banner.getActivation() == 1) {
            banner.setActivation(0);

            // 비활성화 -> 활성화
        } else if (banner.getActivation() == 0) {
            banner.setActivation(1);
        }

        bannerRepository.save(banner);

        return ResponseEntity.ok().body(banner);
    }

    // 관리자 배너 삭제
    public ResponseEntity<?> bannerDelete(int[] bnoArray) {
        log.info("관리자 배너 삭제 Serv 1 : " + Arrays.toString(bnoArray));

        for (int bno : bnoArray) {
            // 상품 배너 삭제 반복
            bannerRepository.deleteById(bno);
        }
        return ResponseEntity.ok().body("ok");
    }

    // 관리자 상품 등록 cate1 조회
    public List<Cate1DTO> findAllCate1() {
        List<Cate1> cate1s = cate1Repository.findAll();
        log.info("관리자 상품 등록 Serv : " + cate1s);
        // 조회된 Entity List -> DTO List
        return cate1s.stream().map(cate1 -> modelMapper.map(cate1, Cate1DTO.class))
                .collect(Collectors.toList());
    }

    // 관리자 상품 등록 cate2 조회
    public ResponseEntity<?> findAllCate2ByCate1(int cate1) {
        // 조회된 Entity List -> DTO List
        List<Cate2DTO> cate2List = cate2Repository.findByCate1(cate1).stream()
                .map(cate2 -> modelMapper.map(cate2, Cate2DTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(cate2List);
    }

    // 관리자 상품 등록 cate3 조회
    public ResponseEntity<?> findAllCate3ByCate2(int cate2) {
        // 조회된 Entity List -> DTO List
        List<Cate3DTO> cate3List = cate3Repository.findByCate2(cate2).stream()
                .map(cate3 -> modelMapper.map(cate3, Cate3DTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(cate3List);
    }

    // 관리자 상품 기본 목록 조회
    public AdminProductPageResponseDTO adminSelectProducts(AdminProductPageRequestDTO adminProductPageRequestDTO) {
        log.info("관리자 상품 목록 조회 Serv 1 : " + adminProductPageRequestDTO);

        Pageable pageable = adminProductPageRequestDTO.getPageable("no");

        // DB 조회
        Page<Product> pageProducts = productRepository.adminSelectProducts(adminProductPageRequestDTO, pageable);
        log.info("관리자 상품 목록 조회 Serv 2 : " + pageProducts);

        // Page<Product>을 List<ProductDTO>로 변환
        List<ProductDTO> dtoList = pageProducts.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        log.info("관리자 상품 목록 조회 Serv 3 : " + dtoList);

        // total 값
        int total = (int) pageProducts.getTotalElements();

        // List<ProductDTO>와 page 정보 리턴
        return AdminProductPageResponseDTO.builder()
                .adminProductPageRequestDTO(adminProductPageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();

    }

    // 관리자 상품 목록 검색 - cate1을 type으로 선택 시 cate1 조회
    public ResponseEntity<?> findCate1s() {
        List<Cate1> cate1s = cate1Repository.findAll();
        return ResponseEntity.ok().body(cate1s);
    }

    // 관리자 상품 검색 목록 조회
    public AdminProductPageResponseDTO adminSearchProducts(AdminProductPageRequestDTO adminProductPageRequestDTO) {
        log.info("관리자 상품 목록 검색 조회 Serv 1 : " + adminProductPageRequestDTO);
        Pageable pageable = adminProductPageRequestDTO.getPageable("no");

        // DB 조회
        Page<Product> pageProducts = productRepository.adminSearchProducts(adminProductPageRequestDTO, pageable);
        log.info("관리자 상품 목록 검색 조회 Serv 2 : " + pageProducts);

        // Page<Product>을 List<ProductDTO>로 변환
        List<ProductDTO> dtoList = pageProducts.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        log.info("관리자 상품 목록 검색 조회 Serv 3 : " + dtoList);

        // total 값
        int total = (int) pageProducts.getTotalElements();

        // List<ProductDTO>와 page 정보 리턴
        return AdminProductPageResponseDTO.builder()
                .adminProductPageRequestDTO(adminProductPageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();

    }

    // 관리자 상품 삭제 - json (상품 수정)
    public void prodArrDelete(String prodNoList) {

        // JON 문자열 파싱 -> int 배열로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        Integer[] prodNoArray = null;
        try {
            prodNoArray = objectMapper.readValue(prodNoList, Integer[].class);
            log.info("관리자 상품 삭제 1 - prodNoArray : " + prodNoArray.toString());

            for (int prodNo : prodNoArray) {
                // 상품 삭제 반복
                log.info("관리자 상품 삭제 2 - prodNo : " + prodNo);
                productRepository.deleteById(prodNo);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 관리자 상품 삭제
    public ResponseEntity<?> prodDelete(int[] prodNoArray) {
        log.info("관리자 상품 삭제 Serv 1 : " + Arrays.toString(prodNoArray));

        for (int prodNo : prodNoArray) {
            // 상품 삭제 반복
            productRepository.deleteById(prodNo);
        }
        return ResponseEntity.ok().body("ok");
    }


    // 관리자 상품 수정 - DB insert
    @Transient
    public ProductDTO modifyProduct(String optionDTOListJson,
                                    ProductDTO productDTO,
                                    MultipartFile thumb190,
                                    MultipartFile thumb230,
                                    MultipartFile thumb456,
                                    MultipartFile detail860) {

        log.info("관리자 상품 수정 service1 productDTO : " + productDTO.toString());
        log.info("관리자 상품 수정 service2 thumb190 : " + thumb190);
        log.info("관리자 상품 수정 service3 thumb230 : " + thumb230);
        log.info("관리자 상품 수정 service4 thumb456 : " + thumb456);
        log.info("관리자 상품 수정 service5 detail860 : " + detail860);
        log.info("관리자 상품 수정 service6 json : " + optionDTOListJson);

        // 이미지 파일 등록 : 해당 디렉토리 없을 경우 자동 생성
        File file = new File(imgUploadPath);
        if (!file.exists()) {
            file.mkdir();
        }
        String path = file.getAbsolutePath();

        // 원본 파일 폴더 자동 생성
        String orgPath = path + "/orgImage";
        File orgFile = new File(orgPath);
        if (!orgFile.exists()) {
            orgFile.mkdir();
        }
        // 저장
        Product saveProduct = new Product();

        // 기존 이미지를 삭제 했다면
        if (productDTO.getThumb1() == null) {
            // 리사이징 함수 호출 - 새 이미지 저장
            String sName190 = imgResizing(thumb190, orgPath, path, 190, 190);
            // 파일 이름 DTO에 저장
            productDTO.setThumb1(sName190);
        }
        if (productDTO.getThumb2() == null) {
            String sName230 = imgResizing(thumb230, orgPath, path, 230, 230);
            productDTO.setThumb2(sName230);
        }
        if (productDTO.getThumb3() == null) {
            String sName456 = imgResizing(thumb456, orgPath, path, 456, 456);
            productDTO.setThumb3(sName456);
        }
        if (productDTO.getDetail() == null) {
            String sName860 = imgResizing(detail860, orgPath, path, 860);
            productDTO.setDetail(sName860);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        List<ColorDTO> optionDTOList = null;
        try {
            ColorDTO[] optionDTOArray = objectMapper.readValue(optionDTOListJson, ColorDTO[].class);
            optionDTOList = Arrays.asList(optionDTOArray);
            log.info(optionDTOList.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // option 정보 Entity에 입력 후 DB 저장
        if (optionDTOList != null) {
            for (ColorDTO option : optionDTOList) {

                // DTO -> Entity : Entity의 영속성 때문에 매번 새로 생성해야함
                Product product = modelMapper.map(productDTO, Product.class);
                log.info("관리자 상품 등록 service8 product : " + product.toString());

                // 상품 정보 DB 저장
                saveProduct = productRepository.save(product);
                log.info("관리자 상품 등록 service10 savedProduct : " + saveProduct.toString());
            }
            // option 없는 경우
        } else {
            // DTO -> Entity
            Product product = modelMapper.map(productDTO, Product.class);
            log.info("관리자 상품 등록 service8 product : " + product.toString());
            // 상품 정보 DB 저장
            saveProduct = productRepository.save(product);
            log.info("관리자 상품 등록 service10 savedProduct : " + saveProduct.toString());

        }
        return modelMapper.map(saveProduct, ProductDTO.class);
    }

    // 관리자 상품 관리 - 등록한 상품 출력
    public ProductDTO prodView(int prodNo) {
        return modelMapper.map(productRepository.findById(prodNo), ProductDTO.class);
    }

    // 관리자 상품 관리 - 등록한 상품 커스텀 옵션 추가
    public ResponseEntity<?> optionAdd(List<OptionDTO> optionDTOS) {
        log.info("상품 의류 옵션 추가 Serv 1 : " + optionDTOS);

        // 옵션 리스트 insert
        for (OptionDTO optionDTO : optionDTOS) {
            Option option = modelMapper.map(optionDTO, Option.class);
            log.info("상품 의류 옵션 추가 Serv 2 : " + option);
            optionRepository.save(option);
        }
        return ResponseEntity.ok().body("option insert");
    }

    // 관리자 게시판 관리 - 기본 게시글 목록 출력
    public AdminBoardPageResponseDTO findBoardByGroup(String cate, AdminBoardPageRequestDTO adminBoardPageRequestDTO) {
        String group = adminBoardPageRequestDTO.getGroup();
        String keyword = adminBoardPageRequestDTO.getKeyword();
        Pageable pageable = adminBoardPageRequestDTO.getPageable("bno");
        log.info("게시판관리 - 목록 Serv 1 : " + adminBoardPageRequestDTO);
        log.info("게시판관리 - 목록 Serv 2 cate : " + cate);

        Page<Tuple> boardEntities = null;

        // 전체 조회
        if ((keyword == null || "".equals(keyword)) && ("".equals(cate) || "all".equals(cate))) {
            // DB 조회
            boardEntities = boardRepository.selectBoardsByGroup(adminBoardPageRequestDTO, pageable, group);
            log.info("게시판관리 - 목록 Serv 3 전체 조회 : " + boardEntities);

            // type이 cate인 검색
        } else if ((keyword == null || "".equals(keyword)) && !"all".equals(cate)) {
            // DB 조회
            boardEntities = boardRepository.searchBoardsByCate(adminBoardPageRequestDTO, pageable, group, cate);
            log.info("게시판관리 - 목록 Serv 4 cate인 검색 : " + boardEntities);

            // 키워드로 검색
        } else if (keyword != null) {
            // DB 조회
            boardEntities = boardRepository.searchBoardsByGroup(adminBoardPageRequestDTO, pageable, group);
            log.info("게시판관리 - 목록 Serv 5 키워드로 검색 : " + boardEntities);
        }


        // Page<Tuple>을 List<BoardDTO>로 변환
        List<BoardDTO> dtoList = boardEntities.getContent().stream()
                .map(tuple -> {
                    log.info("tuple : " + tuple);
                    // Tuple -> Board 테이블 칼럼
                    BoardEntity boardEntity = tuple.get(0, BoardEntity.class);
                    // Tuple -> Join한 typeName 칼럼
                    String typeName = tuple.get(1, String.class);
                    // Tuple -> Join한 cateName 칼럼
                    String cateName = tuple.get(2, String.class);
                    // Tuple -> Join한 cateName 칼럼
                    String nick = tuple.get(3, String.class);
                    // Entity -> DTO
                    BoardDTO boardDTO = modelMapper.map(boardEntity, BoardDTO.class);
                    boardDTO.setTypeName(typeName);
                    boardDTO.setCateName(cateName);
                    boardDTO.setNick(nick);

                    log.info("게시판관리 - 목록 Serv 3 : " + boardDTO);

                    return boardDTO;
                })
                .toList();
        log.info("게시판관리 - 목록 Serv 4 : " + dtoList);

        // total 값
        int total = (int) boardEntities.getTotalElements();

        // List<ProductDTO>와 page 정보 리턴
        return AdminBoardPageResponseDTO.builder()
                .adminBoardPageRequestDTO(adminBoardPageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    // 관리자 게시판 관리 - 게시글 등록 카테고리 조회
    public List<BoardCateDTO> findBoardCate() {

        List<BoardCateEntity> boardCates = boardCateRepository.findAll();
        // 조회된 Entity List -> DTO List
        return boardCates.stream()
                .map(cate -> modelMapper.map(cate, BoardCateDTO.class))
                .collect(Collectors.toList());
    }

    // 관리자 게시판 관리 - 게시글 등록 type(말머리) 조회
    public ResponseEntity<?> findBoardType(String cate) {

        List<BoardTypeEntity> boardTypes = typeRepository.findByCate(cate);
        // 조회된 Entity List -> DTO List
        List<BoardTypeDTO> typeList = boardTypes.stream()
                .map(type -> modelMapper.map(type, BoardTypeDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(typeList);
    }

    // 관리자 게시판 관리 - 게시글 등록
    public void adminBoardWrite(BoardDTO boardDTO) {
        log.info("게시글 등록 Cont 1 : " + boardDTO);
        BoardEntity boardEntity = modelMapper.map(boardDTO, BoardEntity.class);
        boardRepository.save(boardEntity);

        // file 등록 구현되면 할 것

    }

    // 관리자 게시판 관리 - 게시글 삭제
    public ResponseEntity<?> boardDelete(int bno) {
        log.info("관리자 게시글 삭제 Serv 1 : " + bno);

        Optional<BoardEntity> boardEntity = boardRepository.findById(bno);
        log.info("관리자 게시글 삭제 Serv 2 : " + bno);
        // 게시글 아직 있으면
        if (boardEntity.isPresent()) {
            // 게시글 삭제
            boardRepository.deleteById(bno);
            return ResponseEntity.ok().body(boardEntity);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not found");
        }
    }

    // 관리자 게시판 보기
    public BoardDTO selectBoard(int bno) {
        log.info("관리자 게시글 보기 Serv 1 : " + bno);
        // DB 조회
        Optional<BoardEntity> optEntity = boardRepository.findById(bno);
        // Entity
        BoardEntity boardEntity = optEntity.get();
        log.info("관리자 게시글 보기 Serv 2 : " + boardEntity);
        // Entity -> DTO
        BoardDTO boardDTO = modelMapper.map(boardEntity, BoardDTO.class);
        log.info("관리자 게시글 보기 Serv 3 : " + boardDTO);

        // file 조회
        List<BoardFileDTO> boardFileDTOS = fileRepository.findByBno(bno)
                .stream()
                .map(entity -> modelMapper.map(entity, BoardFileDTO.class))
                .toList();

        boardDTO.setFileDTOList(boardFileDTOS);
        return boardDTO;
    }

    // 관리자 회원 목록 (현황) 조회
    public AdminMemberPageResponseDTO selectMembers(AdminMemberPageRequestDTO adminMemberPageRequestDTO) {
        log.info("관리자 회원 목록 Serv 1  ");
        Pageable pageable = adminMemberPageRequestDTO.getPageable("no");

        Page<Member> members = null;
        // 회원 기본 조회
        if (adminMemberPageRequestDTO.getKeyword() == null) {
            // DB 조회
            members = memberRepository.selectMemberList(adminMemberPageRequestDTO, pageable);
            log.info("관리자 회원 기본 목록 Serv 2 : " + members);

            // 회원 검색 조회
        } else {
            // DB 조회
            members = memberRepository.searchMemberList(adminMemberPageRequestDTO, pageable);
            log.info("관리자 회원 검색 목록 Serv 2 : " + members);
        }
        // Page<Entity>을 List<DTO>로 변환
        List<MemberDTO> dtoList = members.getContent().stream()
                .map(member -> modelMapper.map(member, MemberDTO.class))
                .toList();

        log.info("관리자 회원 목록 Serv 3 : " + dtoList);

        // total 값
        int total = (int) members.getTotalElements();

        // List<ProductDTO>와 page 정보 리턴
        return AdminMemberPageResponseDTO.builder()
                .adminMemberPageRequestDTO(adminMemberPageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    // 이미지 리사이징 함수 - width, height
    public String imgResizing(MultipartFile file, String orgPath, String path, int targetWidth, int targetHeight) {
        String oName = file.getOriginalFilename();
        String ext = oName.substring(oName.lastIndexOf("."));

        String sName = UUID.randomUUID().toString() + ext;

        try {
            // 원본 파일 저장
            file.transferTo(new File(orgPath, sName));

            // 리사이징
            Thumbnails.of(new File(orgPath, sName))
                    .size(targetWidth, targetHeight)
                    .toFile(new File(path, sName));

            log.info("이미지 리사이징 완료: " + sName);

            return sName;
        } catch (IOException e) {
            log.error("이미지 리사이징 실패: " + e.getMessage());
            return null;
        }
    }

    // 이미지 리사이징 함수 - width만
    public String imgResizing(MultipartFile file, String orgPath, String path, int targetWidth) {
        String oName = file.getOriginalFilename();
        String ext = oName.substring(oName.lastIndexOf("."));

        String sName = UUID.randomUUID().toString() + ext;

        try {
            // 원본 파일 저장
            file.transferTo(new File(orgPath, sName));

            // 리사이징
            Thumbnails.of(new File(orgPath, sName))
                    .width(targetWidth)
                    .toFile(new File(path, sName));

            log.info("이미지 리사이징 완료: " + sName);

            return sName;
        } catch (IOException e) {
            log.error("이미지 리사이징 실패: " + e.getMessage());
            return null;
        }
    }

}
