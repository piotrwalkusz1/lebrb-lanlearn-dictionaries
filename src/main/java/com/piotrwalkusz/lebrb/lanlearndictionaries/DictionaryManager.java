package com.piotrwalkusz.lebrb.lanlearndictionaries;

import com.piotrwalkusz.lebrb.lanlearn.Language;
import com.piotrwalkusz.lebrb.lanlearn.TranslationDictionary;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DictionaryManager {

    private GridFsTemplate gridFs;

    public DictionaryManager(GridFsTemplate gridFS) {
        this.gridFs = gridFS;
    }

    @PostConstruct
    private void initialization() {
        updateDictionaries();
    }

    private List<TranslationDictionary> dictionaries = Collections.emptyList();

    public void updateDictionaries() {
        dictionaries = Arrays.stream(gridFs.getResources("*"))
                .map(x -> {
                    try {
                        return TranslationDictionary.Companion.createFromReader(new InputStreamReader(x.getInputStream()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public TranslationDictionary getDictionary(Language from, Language to) {
        for (TranslationDictionary dict : dictionaries) {
            if (dict.getSourceLanguage() == from && dict.getDestinationLanguage() == to) {
                return dict;
            }
        }
        return null;
    }

    public List<Pair<Language, Language>> getAllSupportedTranslations() {
        return dictionaries.stream()
                .map(x -> Pair.of(x.getSourceLanguage(), x.getDestinationLanguage()))
                .collect(Collectors.toList());
    }
}