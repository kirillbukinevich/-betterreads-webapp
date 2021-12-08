package io.javabrains.betterreads.search;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.stream.Collectors;

@Controller
public class SearchController {

  private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";

  private final WebClient webClient;

  public SearchController(WebClient.Builder webClientBuilder) {
    this.webClient =
        webClientBuilder
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs(
                        configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                    .build())
            .baseUrl("http://openlibrary.org/search.json")
            .build();
  }

  @GetMapping(value = "/search")
  public String getSearchResults(@RequestParam String query, Model model) {
    var resultMono =
        this.webClient.get().uri("?q={query}", query).retrieve().bodyToMono(SearchResult.class);
    var result = resultMono.block();
    var books =
        result.getDocs().stream()
            .limit(10)
            .map(
                bookResult -> {
                  bookResult.setKey(bookResult.getKey().replace("/works/", ""));
                  var coverId = bookResult.getCover_i();
                  if (StringUtils.hasText(coverId)) {
                    coverId = COVER_IMAGE_ROOT + coverId + "-M.jpg";
                  } else {
                    coverId = "/images/no-image.png";
                  }
                  bookResult.setCover_i(coverId);
                  return bookResult;
                })
            .collect(Collectors.toList());
    model.addAttribute("searchResults", books);
    return "search";
  }
}
