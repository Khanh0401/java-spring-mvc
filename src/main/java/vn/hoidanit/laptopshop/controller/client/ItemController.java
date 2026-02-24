package vn.hoidanit.laptopshop.controller.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.Product_;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.domain.dto.ProductCriteriaDTO;
import vn.hoidanit.laptopshop.domain.dto.RegisterDTO;
import vn.hoidanit.laptopshop.service.ProductService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ItemController {
    private final ProductService productService;

    public ItemController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product/{id}")
    public String getProductPage(Model model, @PathVariable long id) {
        Product get_data = this.productService.fetchProductByID(id).get();
        model.addAttribute("get_data", get_data);
        return "client/product/detail";
    }

    @PostMapping("/add-product-to-cart/{id}")
    public String addProductToCart(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        long product_id = id;
        String email = (String) session.getAttribute("email");

        this.productService.handleAddProductToCart(email, product_id, session, 1);
        return "redirect:/";
    }

    @GetMapping("/cart")
    public String getCartPage(Model model, HttpServletRequest request) {
        User currentUser = new User();
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        Cart cart = this.productService.fetchByUser(currentUser);

        // neu cart rong thi truyen vao ArrayList rong
        List<CartDetail> cartDetails = (cart == null) ? new ArrayList<CartDetail>() : cart.getCartDetails();

        double totalPrice = 0;
        for (CartDetail cd : cartDetails) {
            totalPrice += cd.getPrice() * cd.getQuantity();
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("cart", cart);

        return "client/cart/show";
    }

    @PostMapping("/delete-cart-product/{id}")
    public String deleteCartDetail(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long cartDetailID = id;
        this.productService.handleRemoveCartDetail(cartDetailID, session);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String getCheckOutPage(Model model, HttpServletRequest request) {
        User currentUSer = new User();
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUSer.setId(id);

        Cart cart = this.productService.fetchByUser(currentUSer);
        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();

        double totalPrice = 0;
        for (CartDetail cd : cartDetails) {
            totalPrice += cd.getPrice() * cd.getQuantity();
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);

        return "client/cart/checkout";
    }

    @PostMapping("/confirm-checkout")
    public String getCheckOutPage(@ModelAttribute("cart") Cart cart) {
        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();
        this.productService.handleUpdateCartBeforeCheckout(cartDetails);

        return "redirect:/checkout";
    }

    @PostMapping("/place-order")
    public String handlePlaceOrder(HttpServletRequest request,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress,
            @RequestParam("receiverPhone") String receiverPhone) {

        HttpSession session = request.getSession(false);

        User currentUSer = new User();
        long id = (long) session.getAttribute("id");
        currentUSer.setId(id);

        this.productService.handlePlaceOrder(currentUSer, session, receiverName, receiverAddress, receiverPhone);

        return "redirect:/thankyou";
    }

    @GetMapping("/thankyou")
    public String getThankYouPage(Model model) {

        return "client/cart/thankyou";
    }

    @PostMapping("/add-product-from-view-detail")
    public String handleAddProductFromViewDetail(
            @RequestParam("id") long id,
            @RequestParam("quantity") long quantity,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        String email = (String) session.getAttribute("email");
        this.productService.handleAddProductToCart(email, id, session, quantity);

        return "redirect:/product/" + id;
    }

    @GetMapping("/products")
    public String getProductPage(Model model,
            // @RequestParam("page") Optional<String> pageOptional,
            // @RequestParam("name") Optional<String> nameOptional,
            // @RequestParam("min-price") Optional<String> minOptional,
            // @RequestParam("max-price") Optional<String> maxOptional,
            // @RequestParam("factory") Optional<String> factoryOptional,
            // @RequestParam("price") Optional<String> priceOptional,
            // @RequestParam("sort") Optional<String> sortOptional
            ProductCriteriaDTO productCriteriaDTO,
            HttpServletRequest request) {

        int page = 1;
        try {
            if (productCriteriaDTO.getPage().isPresent()) {
                page = Integer.parseInt(productCriteriaDTO.getPage().get());
            } else {
                // page = 1;
            }
        } catch (Exception e) {
            // page = 1;
        }

        // check sort price
        Pageable pageable = PageRequest.of((page - 1), 3);

        if (productCriteriaDTO.getSort() != null && productCriteriaDTO.getSort().isPresent()) {

            String sort = productCriteriaDTO.getSort().get();

            if (sort.equals("gia-tang-dan")) {
                pageable = PageRequest.of((page - 1), 3, Sort.by(Product_.PRICE).ascending());
            } else if (sort.equals("gia-giam-dan")) {
                pageable = PageRequest.of((page - 1), 3, Sort.by(Product_.PRICE).descending());
            } else {
                pageable = PageRequest.of((page - 1), 3);
            }
        }

        Page<Product> productsPages = this.productService.fetchAllProductsWithSpec(pageable, productCriteriaDTO);

        /*
         * // Old code
         * 
         * // String name = nameOptional.isPresent() ? nameOptional.get() : "";
         * // Page<Product> productsPages =
         * // this.productService.fetchAllProductsWithSpec(pageable, name);
         * 
         * // // min price
         * // double min = minOptional.isPresent() ?
         * Double.parseDouble(minOptional.get())
         * // : 0;
         * // Page<Product> productsPages =
         * // this.productService.fetchAllProductsWithSpec(pageable, min);
         * 
         * // // max price
         * // double max = maxOptional.isPresent() ?
         * Double.parseDouble(maxOptional.get())
         * // : 0;
         * // Page<Product> productsPages =
         * // this.productService.fetchAllProductsWithSpec(pageable, max);
         * 
         * // // max price
         * // String factory = factoryOptional.isPresent() ? factoryOptional.get() : "";
         * // Page<Product> productsPages =
         * // this.productService.fetchAllProductsWithSpec(pageable, factory);
         * 
         * // // factory list
         * // List<String> factory = Arrays.asList(factoryOptional.get().split(","));
         * // Page<Product> productsPages =
         * // this.productService.fetchAllProductsWithSpec(pageable, factory);
         * 
         * // // in range min max
         * // String price = priceOptional.isPresent() ? priceOptional.get() : "";
         * // Page<Product> productsPages =
         * // this.productService.fetchAllProductsWithSpec(pageable, price);
         * 
         * // // list range min max
         * // List<String> price = Arrays.asList(priceOptional.get().split(","));
         * // Page<Product> productsPages =
         * // this.productService.fetchAllProductsWithSpec(pageable, price);
         */

        List<Product> productsList = productsPages.getContent().size() > 0 ? productsPages.getContent()
                : new ArrayList<Product>();

        String qs = request.getQueryString();
        if (qs != null && !qs.isBlank()) {
            // remove page
            qs = qs.replace("page=" + page, "");
        }

        model.addAttribute("products", productsList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPages.getTotalPages());
        model.addAttribute("queryString", qs);

        return "client/product/show";
    }

}
