package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;



/*
* xToOne (ManyToOne, OneToOne) 최적화

 */

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1 (){

        //지연로딩은 null로 들어감 (모듈 사용 시)
        // EAGER은 웬만해서 사용X => 성능 최적화 여지가 없음
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // 강제로 LAZY Loading
            order.getDelivery().getAddress();
        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<OrderSimpleQueryDto> ordersV2() {

        // 반복문 마다 쿼리가 나감 => N+1 문제(주문 1 + 회원 N + 배송 N)
        // 영속성컨텍스트부터 확인하므로 회원이 중복되면 회원은 N번이 아닐 수도 ㅎㅎ

        return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(OrderSimpleQueryDto::new)
                .collect(Collectors.toList());

    }

    @GetMapping("/api/v3/simple-orders")
    public List<OrderSimpleQueryDto> ordersV3() {
        //fetch join => join한 모든 컬럼을 select함 => v4에서 개선
        return orderRepository.findAllWithMemberDelivery().stream().map(
                o -> new OrderSimpleQueryDto(o)
        ).collect(Collectors.toList());
    }


    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {

        // 그럼 V3 vs V4는? tradeoff가 있음. V4는 재사용성 떨어짐 fit하게 만들어버림, 단 v3보다 성능은 좋음
        // repository에 API 스펙이 들어와버림. 계층 붕괴
        return orderSimpleQueryRepository.findOrderDtos();
    }


}
