package io.javabrains.betterreads.book;

import io.javabrains.betterreads.userbooks.UserBooks;
import io.javabrains.betterreads.userbooks.UserBooksPrimaryKey;
import io.javabrains.betterreads.userbooks.UserBooksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class BookController {

  private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";

  private final BookRepository bookRepository;
  private final UserBooksRepository userBooksRepository;

  @GetMapping(value = "/books/{bookId}")
  public String getBook(
      @PathVariable String bookId, Model model, @AuthenticationPrincipal OAuth2User principal) {
    var optionalBook = bookRepository.findById(bookId);
    if (optionalBook.isPresent()) {
      var book = optionalBook.get();
      var coverImageUrl = "/images/no-image.png";
      if (book.getCoverIds() != null && book.getCoverIds().size() > 0) {
        coverImageUrl = COVER_IMAGE_ROOT + book.getCoverIds().get(0) + "-L.jpg";
      }
      model.addAttribute("coverImage", coverImageUrl);
      model.addAttribute("book", book);

      if (principal != null && principal.getAttribute("login") != null) {
        String userId = principal.getAttribute("login");
        model.addAttribute("loginId", principal.getAttribute("login"));
        var key = new UserBooksPrimaryKey();
        key.setBookId(bookId);
        key.setUserId(userId);
        var userBooks = userBooksRepository.findById(key);
        if (userBooks.isPresent()) {
          model.addAttribute("userBooks", userBooks.get());
        } else {
          model.addAttribute("userBooks", new UserBooks());
        }
      }
      return "book";
    }

    return "book-not-found";
  }
}
