package kr.co.lotteon.repository.product;

import kr.co.lotteon.entity.product.Product;
import kr.co.lotteon.repository.custom.ProductRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> ,ProductRepositoryCustom {
}
