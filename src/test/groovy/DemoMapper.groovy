import org.sirenia.repo.anno.KeyGen
import org.sirenia.repo.anno.Table
import org.sirenia.repo.mapper.BaseMapper

@Table(name="t_demo")
@KeyGen(keyProperty="demoId")
interface DemoMapper extends BaseMapper<Demo,Long> {
}