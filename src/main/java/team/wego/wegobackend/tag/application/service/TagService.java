package team.wego.wegobackend.tag.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.tag.domain.entity.Tag;
import team.wego.wegobackend.tag.domain.repository.TagRepository;

@RequiredArgsConstructor
@Service
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public List<Tag> findOrCreateAll(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        Set<String> normalized = tagNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalized.isEmpty()) {
            return List.of();
        }

        List<String> names = new ArrayList<>(normalized);

        List<Tag> existing = tagRepository.findByNameIn(names);
        Map<String, Tag> existingMap = existing.stream()
                .collect(Collectors.toMap(Tag::getName, Function.identity()));

        List<Tag> createTag = names.stream()
                .filter(name -> !existingMap.containsKey(name))
                .map(Tag::create)
                .toList();

        if (createTag.isEmpty()) {
            return existing;
        }

        // 동시 다른 트랜잭션이 같은 태그를 먼저 생성하는 경우를 처리해야 한다.
        try {
            List<Tag> created = tagRepository.saveAll(createTag);

            Map<String, Tag> merged = new HashMap<>(existingMap);
            created.forEach(tag -> merged.put(tag.getName(), tag));

            return names.stream().map(merged::get).toList();

        } catch (DataIntegrityViolationException e) {
            List<Tag> all = tagRepository.findByNameIn(names);
            Map<String, Tag> allMap = all.stream()
                    .collect(Collectors.toMap(Tag::getName, Function.identity()));

            return names.stream().map(allMap::get).toList();
        }
    }

}
