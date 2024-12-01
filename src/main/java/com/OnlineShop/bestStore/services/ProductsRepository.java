package com.OnlineShop.bestStore.services;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.OnlineShop.bestStore.models.Products;


public interface ProductsRepository extends JpaRepository<Products , Integer> {
	
	

}
