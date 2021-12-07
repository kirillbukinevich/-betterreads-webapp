package io.javabrains.betterreads.book;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class BookController {

  private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";

  private final BookRepository bookRepository;

  @GetMapping(value = "/books/{bookId}")
  public String getBook(@PathVariable String bookId, Model model) {
    try {
      var optionalBook = bookRepository.findById(bookId);
      if (optionalBook.isPresent()) {
        var book = optionalBook.get();
        var coverImageUrl = "/images/no-image.png";
        if (book.getCoverIds() != null && book.getCoverIds().size() > 0) {
          coverImageUrl = COVER_IMAGE_ROOT + book.getCoverIds().get(0) + "-L.jpg";
        }
        model.addAttribute("coverImage", coverImageUrl);
        model.addAttribute("book", book);
        return "book";
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "book-not-found";
  }
}
