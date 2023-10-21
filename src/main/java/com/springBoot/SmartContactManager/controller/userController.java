package com.springBoot.SmartContactManager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
// import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.springBoot.SmartContactManager.Dao.ContactRepository;
import com.springBoot.SmartContactManager.Dao.UserRepository;
import com.springBoot.SmartContactManager.Entity.Contact;
import com.springBoot.SmartContactManager.Entity.User;
import com.springBoot.SmartContactManager.helper.Message;
// import com.springBoot.SmartContactManager.services.sessionServices;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/user")                                              //Default Path for a logged in user starts from /user
public class userController {
    

    @Autowired
    private UserRepository userRepository;                            // To use basic CRUD features on User

    @Autowired
    private ContactRepository contactRepository;                      // To use basic CRUD features on Contacts

    @ModelAttribute                                                   //Used to provide a common data to the Model
    private void commonData(Model model, Principal principal){
        String name = principal.getName();                            // return the field that is unique. In this case email is unique.
        User user = userRepository.getUserByUserName(name);           //custom defined function by us in the DAO class.
        model.addAttribute("user", user);
    }

    //Returns the home page after login
    @RequestMapping("/index") 
    public String dashBoard(Model model, Principal principal){        //Principal tells us the username and password of the logged in user
        return "user/user_dashBoard";
    }

    //Returns the contact form
    @GetMapping("/add-contact")
    public String addContactForm(Model model){
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());                 // Creates an empty contact object so that new values can be added
        return "user/contactForm";
    }

    //Handler to save the contact that is send via contact form
    @PostMapping("/process-contact")
    public String addContact(@ModelAttribute Contact contact,                      //Fetching the contact object containing the details of the contact from the model
                                Principal principal,                               //Fetching the user details of the logged in user
                                @RequestParam("profileImage") MultipartFile file,  //Getting the image that is entered
                                HttpSession session){                              //Session to send a message if there is any error
        try {
            String name = principal.getName();                                     //Getting the field that is unique i.e. Email
            User user = userRepository.getUserByUserName(name);                    //Custon query in DAO which returns the User by using the Email
            user.getContacts().add(contact);                                       //Adding the contact to the User
            // contact.setUser(user); //Linking the contact to the User
            //Linking is not reuqired because we used join column in contact entity class
            session.setAttribute("message",new Message("Contact Added Successfully!!", "alert-success"));
            

            if(!file.isEmpty()){                                                    //Checking if the user entered any photo or not
                // upload the file to a folder and set a unique name
                // contact.setImage("SCM"+user.getContacts().size()+file.getOriginalFilename());
                contact.setImage(file.getOriginalFilename());                       //Linking the image to the contact
                File saveFile = new ClassPathResource("static/img").getFile();                  //saving the file in img folder
                // Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"SCM"+user.getContacts().size()+file.getOriginalFilename());
                Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }else{
                contact.setImage("user.png");                                 //setting a default image if the image is not provided
            }
            userRepository.save(user);                                              //saving the user will automatically save the contact because the cascade type all
        } catch (Exception e) {
            e.getStackTrace();
            session.setAttribute("message",new Message("Something Went Wrong!! Try Again.", "alert-danger"));
        }
        return "user/contactForm";
    }

    //Handler to show contact list
    @GetMapping("/show-contact/{page}")                                                             
    public String showContact(@PathVariable("page") Integer page,Model model,Principal principal){
        model.addAttribute("title", "Contact List");
        // String userName = principal.getName();
        // User user = userRepository.getUserByUserName(userName);
        // List<Contact> contacts = user.getContacts();
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);                                            //Defining the maximum number of contacts in a page
        Page<Contact> contacts = contactRepository.findContactByUser(userRepository.getUserByUserName(principal.getName()).getId(),pageable); //Returns pages of contacts
        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPages",contacts.getTotalPages());
        int idx = page*size;
        model.addAttribute("size", idx);
        return "user/show_contacts";
    }

    //Handler to show a specific contact
    @RequestMapping("/contact/{page}/{cid}") 
    public String showContactDetail(@PathVariable("cid") Integer id,@PathVariable("page") Integer page,Model model, Principal principal){
        
        Optional<Contact> contactOptional = contactRepository.findById(id);
        Contact contact = contactOptional.get();

        String name = principal.getName();
        User user = userRepository.getUserByUserName(name);
        if(user.getId()==contact.getUser().getId()){                        //Checking if the contact belong to the user or not
            model.addAttribute("contact", contact);
            model.addAttribute("page", page);
            model.addAttribute("title", contact.getName());
        }
        return "user/contact_detail";
    }

    //Handler to delete a contact
    @GetMapping("/delete/{cid}")
    public String deleteContact(@PathVariable("cid") Integer cid,Model model,Principal principal,HttpSession session){

        Optional<Contact> contactOptional = contactRepository.findById(cid);
        Contact contact = contactOptional.get();
        String name = principal.getName();
        User user = userRepository.getUserByUserName(name);
        if(user.getId()==contact.getUser().getId()){
            contactRepository.deleteById(cid);
            session.setAttribute("message", new Message("Contact deleted successfully!!", "alert-success"));
        }else{
            session.setAttribute("message", new Message("No such contact found", "alert-danger"));
        }
        return "redirect:/user/show-contact/0";
    }

    //Handler to return the update contact form
    @PostMapping("/update-contact/{cid}")
    public String updateForm(Model model, @PathVariable("cid") Integer cid){
        model.addAttribute("title", "Update Contact");
        Contact contact = contactRepository.findById(cid).get();
        model.addAttribute("contact",contact);
        return "user/update_form";
    }

    //Handler to update a contact
    @PostMapping("/process-update")
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session){

        try {
            String name = principal.getName();                                     //Getting the field that is unique i.e. Email
            User user = userRepository.getUserByUserName(name);
            Contact oldContactDetail = contactRepository.findById(contact.getCid()).get();  //Getting the old contact detail before making changes
            if(!file.isEmpty()){

                //deleteing the old image if new image is uploaded
                File deleteFile = new ClassPathResource("static/img").getFile();
                File file1 = new File(deleteFile, oldContactDetail.getImage());
                file1.delete();

                //Saving the new image
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                // Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"SCM"+user.getContacts().size()+file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage("SCM"+user.getContacts().size()+file.getOriginalFilename());    //Saving the new image
            }else{
                contact.setImage(oldContactDetail.getImage());   //saving the old image
            }
            contact.setUser(user);
            contactRepository.save(contact);
            session.setAttribute("message", new Message("Contact Updated Successfully!!", "alert-success"));
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went Wrong!!", "alert-danger"));
        }
        return "redirect:/user/contact/0/"+contact.getCid();
    }

    //handler to open the profile page
    @GetMapping("/profile")
    public String yourProfile(Model model){
        model.addAttribute("title", "Profile Page");
        return "user/profile";
    }

    @GetMapping("/settings")
    public String openSettings(){

        return "user/settings";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                    @RequestParam("newPassword") String newPassword,
                                    @RequestParam("confirmNewPassword") String confirmNewPassword,
                                    HttpSession session,
                                    Principal principal){
        try {
            User user = userRepository.getUserByUserName(principal.getName());
            if(user.getPassword().equals("{noop}"+oldPassword)){
                if(newPassword.equals(confirmNewPassword)){
                    user.setPassword("{noop}"+newPassword);
                    userRepository.save(user);
                    session.setAttribute("message", new Message("Password change successfully!!", "alert-success"));
                }else{
                    session.setAttribute("message", new Message("Conifrmed Password did not match!!", "alert-danger"));
                }
            }else{
                session.setAttribute("message", new Message("Old Password did not match!!", "alert-danger"));
            }
        } catch (Exception e) {
            e.getStackTrace();
            session.setAttribute("message", new Message("Something went wrong, Try again later!!", "alert-danger"));
        }
        
        return "redirect:/user/settings";
    }

    @PostMapping("/update-profile")
    public String updateProfileHandler(@ModelAttribute User user, @RequestParam("profileImage") MultipartFile file, HttpSession session){

        try {
            User oldUserDetail = userRepository.findById(user.getId()).get();  //Getting the old contact detail before making changes
            if(!file.isEmpty()){

                //deleteing the old image if new image is uploaded
                File deleteFile = new ClassPathResource("static/img").getFile();
                File file1 = new File(deleteFile, oldUserDetail.getImageURL());
                file1.delete();

                //Saving the new image
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"SCM"+user.getContacts().size()+""+user.getId()+file.getOriginalFilename());
                // Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"SCM"+user.getContacts().size()+file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                user.setImageURL("SCM"+user.getContacts().size()+""+user.getId()+file.getOriginalFilename());    //Saving the new image
            }else{
                user.setImageURL(oldUserDetail.getImageURL());   //saving the old image
            }
            userRepository.save(user);
            session.setAttribute("message", new Message("Profile Updated Successfully!!", "alert-success"));
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went Wrong!!", "alert-danger"));
        }
        return "redirect:/user/settings";
    }
}
