package io.javabrains.betterreads.home;

import io.javabrains.betterreads.user.BooksByUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

  private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";

  private final BooksByUserRepository booksByUserRepository;

  @GetMapping("/")
  public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {

    if (principal == null || principal.getAttribute("login") == null) {
      return "index";
    }
    String userId = principal.getAttribute("login");

    var booksSLice = booksByUserRepository.findAllById(userId, CassandraPageRequest.of(0, 100));
    var booksByUser = booksSLice.getContent();
    booksByUser =
        booksByUser.stream()
            .map(
                book -> {
                  var coverImageUrl = "/images/no-image.png";
                  if (book.getCoverIds() != null && book.getCoverIds().size() > 0) {
                    coverImageUrl = COVER_IMAGE_ROOT + book.getCoverIds().get(0) + "-M.jpg";
                  }
                  book.setCoverUrl(coverImageUrl);
                  return book;
                })
            .collect(Collectors.toList());

    model.addAttribute("books", booksByUser);
    return "home";
  }
}
