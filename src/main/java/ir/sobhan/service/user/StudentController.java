package ir.sobhan.service.user;

import ir.sobhan.business.DBService.RegistrationDBService;
import ir.sobhan.business.DBService.StudentDBService;
import ir.sobhan.business.exception.NotFoundException;
import ir.sobhan.service.AbstractService.LCRUD;
import ir.sobhan.service.courseSection.model.entity.CourseSectionRegistration;
import ir.sobhan.service.courseSection.model.output.CourseSectionRegistrationOutputDTO;
import ir.sobhan.service.term.model.entity.Term;
import ir.sobhan.service.user.model.entity.User;
import ir.sobhan.service.user.model.input.StudentInputDTO;
import ir.sobhan.service.user.model.output.StudentOutputDTO;
import ir.sobhan.service.user.model.output.TermOfStudentOutputDTO;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("students")
@Slf4j
@Setter
public class StudentController extends LCRUD<User, StudentInputDTO> {
    StudentDBService db;
    final RegistrationDBService registrationDBService;

    public StudentController(StudentDBService dbService, StudentDBService db, RegistrationDBService registrationDBService) {
        super(dbService, StudentOutputDTO.class, (user -> {
            user.setStudent(true);
            user.setActive(true);
        }));
        this.db = db;
        this.registrationDBService = registrationDBService;
    }

    @GetMapping({"{studentId}/grades"})
    ResponseEntity<?> showGrades(@PathVariable Long studentId, @RequestParam(name = "termId") Long termId) {
        Map<String, Object> outputMap = new HashMap<>();

        List<CourseSectionRegistration> registrationList = registrationDBService.findAllBySection_Term_IdAndStudent_Id(termId, studentId);

        List<CourseSectionRegistrationOutputDTO> registrationDTOList = registrationList.stream().map(CourseSectionRegistrationOutputDTO::new).collect(Collectors.toList());

        double ave = registrationList.stream().mapToDouble(CourseSectionRegistration::getScore).average().orElse(0);

        CollectionModel<CourseSectionRegistrationOutputDTO> collectionModel = CollectionModel.of(registrationDTOList);

        outputMap.put("average", ave);
        outputMap.put("sections", collectionModel);

        return ResponseEntity.ok(outputMap);
    }

    @GetMapping({"total-grades"})
    ResponseEntity<?> totalGrades(Authentication authentication) throws NotFoundException {
        User stu = db.getByUsername(authentication.getName());

        Map<Term, Double> aveMap = fillAveMap(stu);

        List<TermOfStudentOutputDTO> termList = new ArrayList<>();

        aveMap.forEach((term, ave) -> termList.add(new TermOfStudentOutputDTO(term, ave)));

        CollectionModel<TermOfStudentOutputDTO> collectionModel = CollectionModel.of(termList);

        return ResponseEntity.ok(collectionModel);
    }

    Map<Term, Double> fillAveMap(User stu) {

        return stu.getStudentInf().getRegistrationSet().stream()
                .collect(Collectors.groupingBy(registration -> registration.getSection().getTerm()))
                .entrySet().stream().map(termListEntry -> Pair.of(termListEntry.getKey(), termListEntry.getValue().stream().mapToDouble(CourseSectionRegistration::getScore).average().orElse(0)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
}