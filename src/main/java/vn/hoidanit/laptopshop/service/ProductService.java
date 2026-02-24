package vn.hoidanit.laptopshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.Product_;
import vn.hoidanit.laptopshop.domain.Role;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.domain.dto.ProductCriteriaDTO;
import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.repository.CartDetailRepository;
import vn.hoidanit.laptopshop.repository.CartRepository;
import vn.hoidanit.laptopshop.repository.OrderDetailRepository;
import vn.hoidanit.laptopshop.repository.OrderRepository;
import vn.hoidanit.laptopshop.repository.ProductRepository;
import vn.hoidanit.laptopshop.repository.RoleRepository;
import vn.hoidanit.laptopshop.service.specification.ProductSpecs;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductService(ProductRepository productRepository,
            CartRepository cartRepository,
            CartDetailRepository cartDetailRepository,
            UserService userService,
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository) {

        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public Page<Product> fetchAllProducts(Pageable pageable) {
        return this.productRepository.findAll(pageable);
    }

    public Product createProduct(Product product) {
        Product products = this.productRepository.save(product);
        return products;
    }

    public Optional<Product> fetchProductByID(long id) {
        return this.productRepository.findOneById(id);
    }

    public void deleteById(long id) {
        this.productRepository.deleteById(id);
    }

    public void handleAddProductToCart(String email, long productID, HttpSession session, long quantity) {

        // check user đã có cart chưa ? nếu chưa -> tạo mới
        User user = this.userService.getUserByEmail(email);

        if (user != null) {
            Cart cart = this.cartRepository.findByUser(user);

            if (cart == null) {
                Cart otherCart = new Cart();
                otherCart.setUser(user);
                otherCart.setSum(0);
                cart = this.cartRepository.save(otherCart);
            }

            // tìm product by id
            Optional<Product> productOptional = this.productRepository.findById(productID);

            if (productOptional.isPresent()) {
                Product realProduct = productOptional.get();

                // check sản phẩm đã từng đc thêm vào giỏ hàng trước đây hay chưa
                CartDetail oldDetail = this.cartDetailRepository.findByCartAndProduct(cart, realProduct);
                if (oldDetail == null) {

                    // lưu cart detail
                    CartDetail cartDetail = new CartDetail();
                    cartDetail.setCart(cart);
                    cartDetail.setProduct(realProduct);
                    cartDetail.setPrice(realProduct.getPrice());
                    cartDetail.setQuantity(quantity);

                    this.cartDetailRepository.save(cartDetail);

                    // update số lượng mặt hàng khác nhau trong cart (sum)
                    int sum = cart.getSum() + 1;
                    cart.setSum(sum);
                    this.cartRepository.save(cart);
                    session.setAttribute("sum", sum); // update số lượng mặt hàng khác nhau trong cart vào session

                } else {

                    oldDetail.setQuantity(oldDetail.getQuantity() + quantity);
                    this.cartDetailRepository.save(oldDetail);
                }
            }
        }
    }

    public Cart fetchByUser(User user) {
        return this.cartRepository.findByUser(user);
    }

    public void handleRemoveCartDetail(long cartDetailID, HttpSession session) {
        Optional<CartDetail> cartDetailOptional = this.cartDetailRepository.findById(cartDetailID);

        if (cartDetailOptional.isPresent()) {
            CartDetail cartDetail = cartDetailOptional.get();

            // tim cart tu cartDetail
            Cart currentCart = cartDetail.getCart();

            // xoa cart detail
            this.cartDetailRepository.deleteById(cartDetailID);

            // up date cart

            if (currentCart.getSum() > 1) {

                // update current cart
                int newSum = currentCart.getSum() - 1;
                currentCart.setSum(newSum);
                session.setAttribute("sum", newSum);
                this.cartRepository.save(currentCart);
            } else {
                // delete cart (sum = 1)
                this.cartRepository.deleteById(currentCart.getId());
                session.setAttribute("sum", 0);
            }
        }
    }

    public void handleUpdateCartBeforeCheckout(List<CartDetail> cartDetails) {
        for (CartDetail cartDetail : cartDetails) {
            Optional<CartDetail> cdOptional = this.cartDetailRepository.findById(cartDetail.getId());
            if (cdOptional.isPresent()) {
                CartDetail currentCartDetail = cdOptional.get();
                currentCartDetail.setQuantity(cartDetail.getQuantity());
                this.cartDetailRepository.save(currentCartDetail);
            }
        }
    }

    /*
     * Để đặt hàng:
     * 1. Tạo order
     * 2. Tạo order_detail từ cart_detail (cart_detail dc lấy từ cart)
     * 3. Xóa cart detail
     * 4. Xóa cart
     */
    public void handlePlaceOrder(User user,
            HttpSession session,
            String receiverName,
            String receiverAddress,
            String receiverPhone) {

        // get cart by user
        Cart cart = this.cartRepository.findByUser(user);
        if (cart != null) {
            List<CartDetail> cartDetails = cart.getCartDetails();

            if (cartDetails != null) {
                // create order
                Order order = new Order();
                order.setUser(user);
                order.setReceiverName(receiverName);
                order.setReceiverAddress(receiverAddress);
                order.setReceiverPhone(receiverPhone);
                order.setStatus("PENDING");

                double sum = 0;
                for (CartDetail cd : cartDetails) {
                    sum += cd.getPrice() * cd.getQuantity();
                }

                order.setTotalPrice(sum);
                order = this.orderRepository.save(order); // lấy order id từ việc lưu

                //// create order_detail ////
                // step 1: lưu cart detail thành order_detail
                for (CartDetail cd : cartDetails) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setProduct(cd.getProduct());
                    orderDetail.setPrice(cd.getPrice());
                    orderDetail.setQuantity(cd.getQuantity());
                    this.orderDetailRepository.save(orderDetail);
                }

                // step 2: delete cart_detail
                for (CartDetail cd : cartDetails) {
                    this.cartDetailRepository.deleteById(cd.getId());
                }

                // step 3: delete cart
                this.cartRepository.deleteById(cart.getId());

                // step 4: update session
                session.setAttribute("sum", 0);
            }
        }
    }

    public Page<Product> fetchAllProductsWithSpec(Pageable pageable, ProductCriteriaDTO productCriteriaDTO) {

        if (productCriteriaDTO.getTarget() == null &&
                productCriteriaDTO.getFactory() == null &&
                productCriteriaDTO.getPrice() == null) {

            return this.productRepository.findAll(pageable);
        }

        Specification<Product> combinedSpec = Specification.where(null);

        if (productCriteriaDTO.getTarget() != null && productCriteriaDTO.getTarget().isPresent()) {
            Specification<Product> currentSpecs = ProductSpecs.matchListTarget(productCriteriaDTO.getTarget().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        if (productCriteriaDTO.getFactory() != null && productCriteriaDTO.getFactory().isPresent()) {
            Specification<Product> currentSpecs = ProductSpecs.matchListFactory(productCriteriaDTO.getFactory().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        if (productCriteriaDTO.getPrice() != null && productCriteriaDTO.getPrice().isPresent()) {
            Specification<Product> currentSpecs = ProductSpecs.matchListFactory(productCriteriaDTO.getPrice().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        return this.productRepository.findAll(combinedSpec, pageable);
    }

    public Specification<Product> buildPriceSpecification(List<String> price) {

        // trong lan chay dau tien combinedSpec = null nen phai them disjunction()
        Specification<Product> combinedSpec = Specification.where(null);

        for (String p : price) {
            double min = 0;
            double max = 0;

            switch (p) {
                case "duoi-10-trieu":
                    min = 1;
                    max = 10000000;
                    break;
                case "10-toi-15-trieu":
                    min = 10000000;
                    max = 15000000;
                    break;
                case "15-toi-20-trieu":
                    min = 15000000;
                    max = 20000000;
                    break;
                case "tren-20-trieu":
                    min = 20000000;
                    max = 30000000;
                    break;
            }

            if (min != 0 && max != 0) {
                Specification<Product> rangSpec = ProductSpecs.matchMultiplePrice(min, max);
                combinedSpec = combinedSpec.or(rangSpec);
            }
        }

        return combinedSpec;
    }

    // public Page<Product> fetchAllProductsWithSpec(Pageable pageable, String name)
    // {
    // return this.productRepository.findAll(ProductSpecs.nameLike(name), pageable);
    // }

    // min price
    // public Page<Product> fetchAllProductsWithSpec(Pageable pageable, double min)
    // {
    // return this.productRepository.findAll(ProductSpecs.minPrice(min), pageable);
    // }

    // // max price
    // public Page<Product> fetchAllProductsWithSpec(Pageable pageable, double max)
    // {
    // return this.productRepository.findAll(ProductSpecs.maxPrice(max), pageable);
    // }

    // // factory
    // public Page<Product> fetchAllProductsWithSpec(Pageable pageable, String
    // factory) {
    // return this.productRepository.findAll(ProductSpecs.matchFactory(factory),
    // pageable);
    // }

    // // list factory
    // public Page<Product> fetchAllProductsWithSpec(Pageable pageable, List<String>
    // factory) {
    // return this.productRepository.findAll(ProductSpecs.matchListFactory(factory),
    // pageable);
    // }

    // // in range min max
    // public Page<Product> fetchAllProductsWithSpec(Pageable pageable, String
    // price) {
    // if (price.equals("10-toi-15-trieu")) {
    // double min = 10000000;
    // double max = 15000000;

    // return this.productRepository.findAll(ProductSpecs.matchPrice(min, max),
    // pageable);
    // } else if (price.equals("15-toi-30-trieu")) {
    // double min = 15000000;
    // double max = 30000000;

    // return this.productRepository.findAll(ProductSpecs.matchPrice(min, max),
    // pageable);
    // } else
    // return this.productRepository.findAll(pageable);
    // }

    // // list range min max
    // public Page<Product> fetchAllProductsWithSpec(Pageable pageable, List<String>
    // price) {

    // // trong lan chay dau tien combinedSpec = null nen phai them disjunction()
    // Specification<Product> combinedSpec = (root, query, criteriaBuilder) ->
    // criteriaBuilder.disjunction();
    // int count = 0;

    // for (String p : price) {
    // double min = 0;
    // double max = 0;

    // switch (p) {
    // case "10-toi-15-trieu":
    // min = 10000000;
    // max = 15000000;
    // count++;
    // break;
    // case "15-toi-20-trieu":
    // min = 15000000;
    // max = 20000000;
    // count++;
    // break;
    // case "20-toi-30-trieu":
    // min = 20000000;
    // max = 30000000;
    // count++;
    // break;
    // }

    // if (min != 0 && max != 0) {
    // Specification<Product> rangSpec = ProductSpecs.matchMultiplePrice(min, max);
    // combinedSpec = combinedSpec.or(rangSpec);
    // }
    // }

    // // check if any price ranges were added (combinedSpec is empty)
    // if (count == 0) {
    // return this.productRepository.findAll(pageable);
    // }

    // return this.productRepository.findAll(combinedSpec, pageable);
    // }
}
