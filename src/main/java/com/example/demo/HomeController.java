package com.example.demo;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private UserService userService;
    @Autowired
    UserRepository userRepository;


    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @PostMapping("/register")
    public String processRegistrationPage(@Valid @ModelAttribute("user") User user, BindingResult result,
                           Model model) {
                      model.addAttribute("user", user);
                      if (result.hasErrors())
                      {
                          return "registration";
                      }
                      else
                      {
                          userService.saveUser(user);
                          model.addAttribute("message", "User Account Created");
                      }
                      return "index";
    }


    @RequestMapping("/")
    public String index(Model model, Principal principal, Authentication authentication)
    {
        model.addAttribute("categories", carRepository.findAll());
        model.addAttribute("cars", carRepository.findAll());
        String username = null;
        try {
            username = principal.getName();
            model.addAttribute("car_user_id", userRepository.findByUsername(principal.getName()).getId());
            return "index1";
        } catch (Exception e){
            model.addAttribute("car_user_id", 0);
            return "index1";
        }
    }

    @RequestMapping("/login")
    public String login(){
        return "login";
    }

    @RequestMapping("/admin")
    public String admin() {return "admin";}

//    @RequestMapping("/secure")
//    public String admin(){
//        return "secure";
//    }


    @RequestMapping("/secure")
    public String secure(Principal principal, Model model){
        String username = principal.getName();
        model.addAttribute("user", userRepository.findByUsername(username));
        return "secure";
    }


    /*
--------------------------------------
    //Combining car homcontroller
    ---------------------------------------------
    */

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    CloudinaryConfig cloudc;


    @RequestMapping("/index1")
    public String index(Model model){
        model.addAttribute("cars", carRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        return "index1";
    }

    @GetMapping("/addCategory")
    public String addCategory(Model model){
        model.addAttribute("category", new Category());
        return "categoryform";
    }

    @PostMapping("/processCategory")
    public String processCategory(@ModelAttribute Category category){
        categoryRepository.save(category);
        return "redirect:/index1";
    }


    @GetMapping("/addCar")
    public String addCar(Model model, Principal principal) {
        model.addAttribute("car", new Car());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("user_id",userRepository.findByUsername(principal.getName()).getId());
        return "carform";
    }


    @PostMapping("/processCar")
    public String processCar(@Valid @ModelAttribute Car car, BindingResult result,
                             @RequestParam("category") long id, @RequestParam("file") MultipartFile file,
                             @RequestParam("car_user_id") long car_user_id, Principal principal) {
        if (result.hasErrors()) {
            return "carform";
        }

        if (file.isEmpty()) {
//            carRepository.save(car);
//            return "redirect:/index1";

        } else {
            try {
                Map uploadResult = cloudc.upload(file.getBytes(),
                        ObjectUtils.asMap("resourcetype", "auto"));
                car.setPicture(uploadResult.get("url").toString());
                carRepository.save(car);
            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/addcar";
            }
        }
//        carRepository.save(car);
//        return "redirect:/index1";



        User user = userRepository.findById(car_user_id).get();
        car.setUser(user);

        Category category = categoryRepository.findById(id).get();
        car.setCategory(category);
        carRepository.save(car);
        return "redirect:/index1";

    }



    @RequestMapping("/detail/{id}")
    public String showCar(@PathVariable("id") long id, Model model)
    {
        model.addAttribute("car", carRepository.findById(id).get());
        model.addAttribute("categories", categoryRepository.findAll());
        return "show";
    }

    @RequestMapping("/update/{id}")
    public String updateCar(@PathVariable("id") long id,
                            Model model, Principal principal ){
        model.addAttribute("car", carRepository.findById(id).get());
        model.addAttribute("user_id", userRepository.findByUsername(principal.getName()).getId());
        Car car = carRepository.findById(id).get();
        model.addAttribute("car", car);

        model.addAttribute("categories", categoryRepository.findAll());

        return "carform";
    }

    @RequestMapping("/delete/{id}")
    public String deleteCar(@PathVariable("id") long id){
        carRepository.deleteById(id);
        return "redirect:/";
    }




    @RequestMapping("/categorydetail/{id}")
    public String showCategory(@PathVariable("id") long id, Model model)
    {
        model.addAttribute("category", categoryRepository.findById(id).get());
        model.addAttribute("cars", carRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        return "showcategory";
    }



}
