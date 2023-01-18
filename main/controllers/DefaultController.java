package main.controllers;

import main.dto.DtoResult;
import main.dto.DtoResultSearch;
import main.dto.DtoStatistics;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import service.ParseSiteOrPage;

import java.io.IOException;
import java.sql.SQLException;


@Controller
public class DefaultController {


//        @RequestMapping("/")
//    public String index() {
//        return "index";
//    }

    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<?> getAllStatistics() {

        try {
            return ResponseEntity.ok().body(new DtoStatistics());
        } catch (Exception ex) {
            return ResponseEntity.ok().body(new DtoResult("Ошибка получения статистики: " +
                    ex.getMessage()));
        }

    }

    @GetMapping("/startIndexing")
    @ResponseBody
    public ResponseEntity<DtoResult> startIndexing() {
        if(!ParseSiteOrPage.getIndexing()) {
            ParseSiteOrPage.startIndexing();
            return ResponseEntity.ok().body(new DtoResult());
        } else {
            return ResponseEntity.ok().body(new DtoResult("Индексация уже запущена!"));
        }

    }

    @GetMapping("/stopIndexing")
      @ResponseBody
    public ResponseEntity<DtoResult> stopIndexing() {
        if(ParseSiteOrPage.getIndexing()) {
            ParseSiteOrPage.stopIndexing();
            return ResponseEntity.ok().body(new DtoResult());
        } else {
            return ResponseEntity.ok().body(new DtoResult("Индексация не запущена!"));
        }

    }

    @PostMapping("/indexPage")
    public ResponseEntity<DtoResult> indexPage(String url) {
        if(ParseSiteOrPage.getIndexing()) {
            return ResponseEntity.ok().body(new DtoResult("Выполняется индексация на данный момент!"));
        } else {
            if (ParseSiteOrPage.checkUrlForReindexing(url)) {
                return ResponseEntity.ok().body(new DtoResult(true));
            } else {
                return ResponseEntity.ok().body(new DtoResult("Данная страница находится за пределами сайтов, " +
                        "указанных в конфигурационном файле"));
            }
        }
    }


    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<?> search(String site, String query) throws SQLException, IOException {

        if(query.isEmpty()) {
            return ResponseEntity.ok().body(new DtoResult("Пустой запрос поиска!"));
        }
        try {
                return ResponseEntity.ok().body(new DtoResultSearch(site, query));
        } catch (Exception ex) {
                return ResponseEntity.ok().body(new DtoResult("ERROR: " + ex.getMessage()));
            }

    }



}
