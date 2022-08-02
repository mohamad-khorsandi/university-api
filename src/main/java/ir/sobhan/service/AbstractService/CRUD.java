package ir.sobhan.service.AbstractService;

import ir.sobhan.business.exception.EntityNotFoundException;
import ir.sobhan.service.AbstractService.model.input.InputDTO;
import ir.sobhan.service.AbstractService.model.output.OutPutDTO;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@AllArgsConstructor
@RequiredArgsConstructor
@Setter
abstract public class CRUD<ENTITY, INPUT_DTO extends InputDTO<ENTITY>>{
    public CRUD(JpaRepository<ENTITY, Long> repository, Class<? extends OutPutDTO<ENTITY>> outDTOClass, Predicate<ENTITY> getFilter, Consumer<ENTITY> postInitializer) {
        this.repository = repository;
        this.outDTOClass = outDTOClass;
        this.getFilter = getFilter;
        this.postInitializer = postInitializer;
    }

    final protected JpaRepository<ENTITY, Long> repository;
    final protected Class<? extends OutPutDTO<ENTITY>> outDTOClass;
    protected Predicate<ENTITY> getFilter = entity -> true;
    protected Consumer<ENTITY> postInitializer = entity -> {};
    String idFieldName = "id";

    @GetMapping({"{id}"})
    ResponseEntity<?> read(@PathVariable Long id) throws Exception {
        ENTITY entity = repository.findById(id).filter(getFilter).orElseThrow(() -> new EntityNotFoundException(id));

        EntityModel<?> userEntityModel = toOutDTOModel(entity);
        return ResponseEntity.ok(userEntityModel);
    }

    @PostMapping
    ResponseEntity<?> create(@RequestBody INPUT_DTO dtoInputEntity) throws Exception{
        ENTITY entity = dtoInputEntity.toRealObj(null);

        postInitializer.accept(entity);
        repository.save(entity);

        EntityModel<?> entityModel = toOutDTOModel(entity);
        return ResponseEntity.created(linkTo(methodOn(CRUD.class).read(getIdOfEntity(entity))).toUri()).body(entityModel);
    }

    @PutMapping("{id}")
    ResponseEntity<?> update(@RequestBody INPUT_DTO inputEntity, @PathVariable Long id) throws Exception {
        ENTITY entity = repository.findById(id).filter(getFilter)
                .orElseThrow(() -> new EntityNotFoundException(id));
        inputEntity.toRealObj(entity);

        repository.save(entity);

        EntityModel<?> entityModel = toOutDTOModel(entity);
        return ResponseEntity.ok(entityModel);
    }

    @DeleteMapping("{id}")
    ResponseEntity<?> delete(@PathVariable Long id){
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Long getIdOfEntity(ENTITY entity) throws IllegalAccessException, NoSuchFieldException {

        Field field = entity.getClass().getDeclaredField(idFieldName);
        field.setAccessible(true);
        return (Long) field.get(entity);
    }

    protected EntityModel<? extends OutPutDTO<ENTITY>> toOutDTOModel(ENTITY entity) throws Exception {
        Constructor constructor = outDTOClass.getConstructor(entity.getClass());
        OutPutDTO<ENTITY> outPutDTO = (OutPutDTO)constructor.newInstance(entity);

        return (EntityModel<? extends OutPutDTO<ENTITY>>)outPutDTO.toModel();
    }
}