package com.rif.categories.service;

import com.rif.authentication.exceptions.UserNotFoundException;
import com.rif.authentication.models.User;
import com.rif.authentication.repositorys.UserRepository;
import com.rif.categories.dto.CategoryRequest;
import com.rif.categories.dto.CategoryResponse;
import com.rif.categories.exceptions.*;
import com.rif.categories.entity.Category;
import com.rif.categories.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository repository;
    private  final UserRepository userRepository;

    // Declare the path folder that we have created to store categories images in the server
    private final static String PATH_CATEGORY_FOLDER = "D:\\CrowdFunding-RIF\\category_images\\";


    // CREATE : add new category
    public Category add(MultipartFile file, String name,Principal principal) throws IOException, CategroyFileException, CategoryExistWithNameAndImage, CategoryNameExistException, CategoryNotNull {
        // get the specific path of the file
        String pathFile = PATH_CATEGORY_FOLDER + file.getOriginalFilename();
        validateCategoryFields(pathFile, name);
        validateIsNotNull(name);
        String userEmail=principal.getName();
        User admin=this.getUserByEmail(userEmail);
        var category = Category.builder()
                .name(name)
                .imagePath(pathFile)
                .creator(admin).build();
        this.repository.save(category);
        // transfer the file (image) to the current server folder
        file.transferTo(new File(pathFile));
        return category;
    }

    // UPDATE : update Category
    public Category update(MultipartFile file, CategoryRequest request, Long id,Principal principal) throws IOException, CategoryNotFoundException, CategroyFileException, CategoryExistWithNameAndImage, CategoryNameExistException, CategoryExistException, UserNotAuthorizedToThisCategory, CategoryNotNull {
        // get the specific path of the file
        String pathFile = PATH_CATEGORY_FOLDER + file.getOriginalFilename();
        Optional<Category> optCategory = this.repository.findById(id);
        User currentAdmin = this.getUserByEmail(principal.getName());

        if (optCategory.isPresent()) {
            Category category = optCategory.get();
            if(currentAdmin.getId().equals(category.getCreator().getId())){
                checkIfNameOrImageExistWhenUpdateCategory(request, category, pathFile);
                validateIsNotNull(request.getName());
                // delete the current image if it exists
                String currentImagePath = optCategory.get().getImagePath();
                if (currentImagePath != null) {
                    Path currentImagePathPath = Paths.get(currentImagePath);
                    Files.deleteIfExists(currentImagePathPath);
                }
                // Update the category's name and image path
                category.setName(request.getName());
                category.setImagePath(pathFile);
                category.setCreator(currentAdmin);
                this.repository.save(category);
                // transfer the file (image) to the current server folder
                file.transferTo(new File(pathFile));
                return category;
            }
            throw new CategoryNotFoundException(String.format("Category Not found with this id : %d to updated !!", id));
            }
            throw new UserNotAuthorizedToThisCategory("This category not belong to this admin to updated");
    }


    // GET : get category by id
    @Transactional
    public CategoryResponse getById(Long id) throws CategoryNotFoundException, IOException {
        Optional<Category> optionalCategory = this.repository.findById(id);
        if (optionalCategory.isPresent()) {
            CategoryResponse currentCategory = new CategoryResponse();
            currentCategory.setCategory_id(optionalCategory.get().getId());
            currentCategory.setName(optionalCategory.get().getName());
            currentCategory.setCreatedAt(optionalCategory.get().getCreatedAt());
            currentCategory.setUpdatedAt(optionalCategory.get().getUpdatedAt());
            // get the current path and convert the file into byte array
            String filePathImage = optionalCategory.get().getImagePath();
            byte[] image = Files.readAllBytes(new File(filePathImage).toPath());
            currentCategory.setImage(Base64.getEncoder().encodeToString(image));
            return currentCategory;
        }
        throw new CategoryNotFoundException(String.format("Category with this id : %d not found", id));

    }

    // DELETE : delete category by id that belong to the user(admin) created it
    public String delete(Long id, Principal principal) throws CategoryNotFoundException, UserNotAuthorizedToThisCategory {
        Optional<Category> optionalCategory = this.repository.findById(id);
        User admin= this.getUserByEmail(principal.getName());
        if (optionalCategory.isPresent()) {
            if(admin.getId().equals(optionalCategory.get().getCreator().getId())){
                Category category = optionalCategory.get();
                deleteFileFromFileSystem(category.getImagePath());
                this.repository.deleteById(id);
                return String.format("Category successfully deleted with id : %d", id);
            }throw new UserNotAuthorizedToThisCategory("This category not belong to this admin to deleted it !!!");

        } else {
                throw new CategoryNotFoundException(String.format("Category with this id : %d not found to deleted", id));
        }
    }

    // delete image from file system in the server
    private void deleteFileFromFileSystem(String imagePath) {
        try {
            Files.deleteIfExists(Paths.get(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // get all categories
    public List<CategoryResponse> getAll() {
        List<CategoryResponse> allCategoriesResponse = new ArrayList<>();
        List<Category> categories = this.repository.findAll();

        for (Category category : categories) {
            String filePathImage = category.getImagePath();
            byte[] image = null;
            try {
                // Convert image to byte array
                image = Files.readAllBytes(Paths.get(filePathImage));
                // Encode image to Base64
                String base64Image = Base64.getEncoder().encodeToString(image);
                // Create CategoryResponse object
                CategoryResponse categoryResponse = new CategoryResponse(
                        category.getId(),
                        category.getUpdatedAt(),
                        category.getCreatedAt(),
                        base64Image,
                        category.getName()
                );
                allCategoriesResponse.add(categoryResponse);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return allCategoriesResponse;
    }


    // create Validations for category image : validateImage method
    public void validateImage(MultipartFile file) throws IOException, CategroyFileException {
        String pathFile = PATH_CATEGORY_FOLDER + file.getOriginalFilename();
        // Validate image type
        String contentType = file.getContentType();
        // validate image not be empty
        if (file.getBytes().length == 0) {
            throw new CategroyFileException("Image must not be empty");
        }

        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp")) {
            throw new CategroyFileException("Invalid image format");
        }
        // Validate path lentgh
        if (pathFile.length() > 80) { // Adjust the length as needed
            if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
                throw new CategroyFileException("Invalid image format");
            }
        }
    }

    // search category by name :: to update in progress of project
    public List<Category> search(String content) throws CategoryNotFoundException {
        List<Category> categories = this.repository.findByNameContaining(content);
        if (categories.isEmpty()) {
            throw new CategoryNotFoundException("Category not found with this name : " + content);
        }
        return categories;
    }

    // validate category fields (name & image)
    public void validateCategoryFields(String imagePath, String name) throws CategoryExistWithNameAndImage, CategroyFileException, CategoryNameExistException, IOException {
        // Normalize input by trimming whitespace and converting to lowercase
        String newName = StringUtils.trimAllWhitespace(name.toLowerCase());
        String pathImage = StringUtils.trimAllWhitespace(imagePath).toLowerCase();

        // Retrieve all categories to compare
        List<Category> categories = repository.findAll();

        for (Category category : categories) {
            String categoryName = StringUtils.trimAllWhitespace(category.getName().toLowerCase());
            String categoryImagePath = StringUtils.trimAllWhitespace(category.getImagePath().toLowerCase());


            if (pathImage.equals(categoryImagePath) && areImagesEqual(imagePath, category.getImagePath())) {
                for (Category oneCategory : categories) {
                    if (newName.equals(StringUtils.trimAllWhitespace(oneCategory.getName().toLowerCase()))) {
                        throw new CategoryExistWithNameAndImage("Both image and name already exist in the database.");
                    }
                }
                {
                    throw new CategroyFileException("Image already exists in the database.");
                }
            } else if (newName.equals(categoryName)) {
                for (Category oneCategory : categories) {
                    if (pathImage.equals(StringUtils.trimAllWhitespace(oneCategory.getImagePath().toLowerCase())) &&
                            areImagesEqual(imagePath, oneCategory.getImagePath())) {
                        throw new CategoryExistWithNameAndImage("Both image and name already exist in the database.");
                    }
                }
                throw new CategoryNameExistException("Name already exists in the database.");
            }
        }
    }

    // Method to compare two images
    public static boolean areImagesEqual(String serverImagePath, String clientImagePath) throws IOException {
        byte[] serverImage = convertImageToByteArray(serverImagePath);
        byte[] clientImage = convertImageToByteArray(clientImagePath);
        return Arrays.equals(serverImage, clientImage);
    }

    // Method to convert image path to byte array
    public static byte[] convertImageToByteArray(String imagePath) throws IOException {
        return Files.readAllBytes(Paths.get(imagePath));
    }

    // Check if project name or image exist when we update project (check for Uppercase and Lowercase and spaces for inputs fields)
    // 1//// check if name and image both exist in db
    // 2//// the image and name of this category will be updated if the client send to db the same name or same image
    // 3//// the image and the name will be accepted to be updated if the name and the image not exist in db
    public void checkIfNameOrImageExistWhenUpdateCategory(CategoryRequest request, Category editedCategory, String pathFile) throws CategoryExistException, CategroyFileException, CategoryExistWithNameAndImage, IOException {
        // Normalize input by trimming whitespace and converting to lowercase
        String newName = StringUtils.trimAllWhitespace(request.getName().toLowerCase());
        String pathImage = StringUtils.trimAllWhitespace(pathFile).toLowerCase();
        String editedName = StringUtils.trimAllWhitespace(editedCategory.getName().toLowerCase());
        String editedPathImage = StringUtils.trimAllWhitespace(editedCategory.getImagePath()).toLowerCase();
        List<Category> categories = this.repository.findAll();
        for (Category category : categories) {
            // Normalize input by trimming whitespace and converting to lowercase
            String categoryName = StringUtils.trimAllWhitespace(category.getName().toLowerCase());
            String categoryPathImage = StringUtils.trimAllWhitespace(category.getImagePath()).toLowerCase();

            if (!newName.equals(editedName) && !editedPathImage.equals(pathImage)) {
                // check if image & name both exist
                if (categoryName.equals(newName)) {
                    for (Category oneCategory : categories) {
                        if (StringUtils.trimAllWhitespace(oneCategory.getImagePath().toLowerCase()).equals(pathImage) &&
                                areImagesEqual(oneCategory.getImagePath(), pathImage)) {
                            throw new CategoryExistWithNameAndImage("This category name and image exist,please enter another name & image to updated");
                        }
                    }
                }
            }
            // check every category name if equals the name entered by the client
            if (categoryName.equals(newName)) {
                // then check if the name requested by client is not equals this updated category name
                if (!newName.equals(editedName)) {
                    throw new CategoryExistException(String.format("This name :%s exist,Please  enter another name", request.getName()));
                }
            }
        }
    }

    // validate name is not null with spacing and
    public void validateIsNotNull(String name) throws CategoryNotNull {
        String nameIsNull= StringUtils.trimAllWhitespace(name.toLowerCase());
        if(nameIsNull.equals("null")){
            throw new CategoryNotNull("Category must not be null");
        }
    }

    // get the authenticated user by email
    public User getUserByEmail(String userEmail) {
        Optional<User> optionalUser=this.userRepository.findByEmail(userEmail);
        throw new UserNotFoundException("User not found !!!");
    }

    
    // get the authenticated user by id
    public List<CategoryResponse> findByUserId(Long id) throws CategoryNotFoundException {
        Optional<User> optionalUser=this.userRepository.findById(id);
        if(optionalUser.isPresent()){
            List<Category> userCategories = this.repository.findByCreatorId(id);
        }throw new CategoryNotFoundException("This user id not exist !!!");
    }
}