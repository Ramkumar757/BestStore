package com.OnlineShop.bestStore.controller;

import java.io.InputStream;
import java.nio.file.*;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.OnlineShop.bestStore.models.Products;
import com.OnlineShop.bestStore.models.ProductsDto;
import com.OnlineShop.bestStore.services.ProductsRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsRepository repo;

    @GetMapping({"", "/"})
    public String showProductsList(Model model) {
        List<Products> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
    	ProductsDto productsDto = new ProductsDto();
        model.addAttribute("productsDto",productsDto );
        return "products/createProducts";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductsDto productsDto, BindingResult result) {
        if (productsDto.getImageFile().isEmpty()) {
            result.addError(new FieldError("productsDto", "imageFile", "The image file is required."));
        }

        if (result.hasErrors()) {
            return "products/createProducts";
        }

        MultipartFile image = productsDto.getImageFile();
        String storageFileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        String uploadDir = "public/images/";

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.err.println("File upload error: " + e.getMessage());
        }

        Products product = new Products();
        product.setName(productsDto.getName());
        product.setBrand(productsDto.getBrand());
        product.setCategory(productsDto.getCategory());
        product.setPrice(productsDto.getPrice());
        product.setDescription(productsDto.getDescription());
        product.setCreatedAt(new Date());
        product.setImageFile(storageFileName);

        repo.save(product);
        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {
        try {
            Products product = repo.findById(id).orElseThrow(() -> new Exception("Product not found"));
            ProductsDto productsDto = new ProductsDto();
            productsDto.setName(product.getName());
            productsDto.setBrand(product.getBrand());
            productsDto.setCategory(product.getCategory());
            productsDto.setPrice(product.getPrice());
            productsDto.setDescription(product.getDescription());

            model.addAttribute("product", product);
            model.addAttribute("productsDto", productsDto);
        } catch (Exception e) {
            System.err.println("Error fetching product: " + e.getMessage());
            return "redirect:/products";
        }

        return "products/editProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(@RequestParam int id, @Valid @ModelAttribute ProductsDto productsDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("productsDto", productsDto);
            return "products/editProduct";
        }

        try {
            Products product = repo.findById(id).orElseThrow(() -> new Exception("Product not found"));

            if (!productsDto.getImageFile().isEmpty()) {
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir).resolve(product.getImageFile());

                try {
                    Files.deleteIfExists(oldImagePath);
                } catch (Exception e) {
                    System.err.println("Error deleting old image: " + e.getMessage());
                }

                MultipartFile newImage = productsDto.getImageFile();
                String storageFileName = System.currentTimeMillis() + "_" + newImage.getOriginalFilename();

                try (InputStream inputStream = newImage.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir).resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFile(storageFileName);
            }

            product.setName(productsDto.getName());
            product.setBrand(productsDto.getBrand());
            product.setCategory(productsDto.getCategory());
            product.setPrice(productsDto.getPrice());
            product.setDescription(productsDto.getDescription());

            repo.save(product);
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {
        try {
            Products product = repo.findById(id).orElseThrow(() -> new Exception("Product not found"));
            Path imagePath = Paths.get("public/images/").resolve(product.getImageFile());

            try {
                Files.deleteIfExists(imagePath);
            } catch (Exception e) {
                System.err.println("Error deleting product image: " + e.getMessage());
            }

            repo.delete(product);
        } catch (Exception e) {
            System.err.println("Error deleting product: " + e.getMessage());
        }

        return "redirect:/products";
    }
}
