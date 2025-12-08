package team.wego.wegobackend.tag.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.wego.wegobackend.tag.domain.repository.TagRepository;

@RequiredArgsConstructor
@Service
public class TagService {

    private final TagRepository tagRepository;


}
