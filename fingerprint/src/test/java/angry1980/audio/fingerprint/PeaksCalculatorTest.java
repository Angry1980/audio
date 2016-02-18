package angry1980.audio.fingerprint;

import angry1980.audio.ClassPathAdapter;
import angry1980.audio.model.*;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PeaksCalculatorTest {

    private static Map<Long, long[]> hashes = ImmutableMap.of(
        1L, new long[]{17017008640L, 17012808640L, 21417008640L, 17012808640L, 17212808240L, 20614809640L, 23411207240L, 20212809840L, 18411608640L, 22412808240L, 17416607240L, 19212806840L, 18411204240L, 23213205240L, 19011804240L, 21215807040L, 19214806040L, 21216606040L, 17815405640L, 21611008640L, 17016804840L, 22412405640L, 23217009840L, 17013009440L, 17214005640L, 18013604040L, 23014408040L, 23212808840L, 20212608640L, 21014806040L, 21011208640L, 20415004240L, 21212805640L, 17013808640L, 23412804240L, 21611004240L, 19014209840L, 19816608440L, 17212009440L, 23416406040L, 21617004040L, 19216609640L, 21813206440L, 23014009040L, 17415006840L, 20012807840L, 23015410440L, 17812809640L, 19615208440L, 17816210040L, 20410806440L, 17011608840L, 21813607040L, 19815407240L, 21812609040L, 21010610240L, 21212808640L, 17411004240L, 23416008040L, 19013009040L, 21216010240L, 20415608240L, 17015409040L, 19012810240L, 19815804840L, 18817005440L, 17011208440L, 20217004640L, 22814004640L, 21413410240L, 22216409240L, 21616405240L, 18813410440L, 19016804040L, 21412807440L, 18412806440L, 17210804240L, 19014409240L, 17814610240L, 17416207840L, 23415407440L, 21415609640L, 20012810240L, 18615809440L, 17016006440L, 17813007840L, 20013804240L, 23011604240L, 23216605240L, 18811607840L, 23412609840L, 20414005040L, 17010606640L, 20814804840L, 22217010240L, 18612606840L, 18014804040L, 21415207440L, 22214604840L, 21412606840L, 20816805640L, 22615008040L, 19412608840L, 22016010440L, 22414205640L, 21612806240L, 21412604440L, 21214204240L, 21015010440L, 22612804240L, 23412408640L, 21010604240L, 21215607040L, 21413008640L, 18616208840L, 17212808640L, 18617007840L, 23212809640L, 17017004640L, 19013004440L, 23212208640L, 21016807240L, 22411608640L, 20016404640L, 18613208840L, 22012810040L, 20214007040L, 23415404840L, 18211809440L, 19413408640L, 21010604840L, 19011810240L, 22815804640L, 17413409240L, 22212805440L, 23014208840L, 21611805440L, 18414205440L, 20210604840L, 18410804240L, 22213609840L, 18012608840L, 23013205840L, 17415205040L, 21411609640L, 22012809040L, 22215010240L, 18814009840L, 21215406040L, 19612205040L, 21010807240L, 18812804240L, 21612204240L, 21617004440L, 23010607040L, 23016405240L, 19812804440L, 21411608040L, 21410605040L, 17012808640L, 21415209840L, 21212808640L, 21415008640L, 17214209640L, 22812809040L, 18014008440L, 20415008440L, 17214808640L, 21412804240L, 21616010040L, 22415205240L, 20212607640L, 17816208640L, 21416009840L, 21613808640L, 21413205640L, 21812206240L, 22411408640L, 18814607640L, 21016608640L, 22213604240L, 21811604440L, 20814207040L, 22214808840L, 19412807240L, 23213204240L, 17413006840L, 18413404240L, 17617004240L, 18815005840L, 19810808240L, 20616806840L, 20212607640L, 20016808440L, 23417010040L, 20415207240L, 20215406440L, 23011410440L, 21410608440L, 23211606040L, 22016204840L, 21614008440L, 22216604640L, 21417004040L, 19612809040L, 22814808440L, 20212804240L, 18412804240L, 19013405440L, 21812208840L, 17215807640L, 17416609840L, 17011407640L, 17213808240L, 23017006840L, 21214206640L, 22812807040L, 17214407640L, 21214810040L, 22615208640L, 19612804840L, 17212007840L, 21015606840L, 19212809840L, 21215004440L, 23013005240L, 22611008040L, 22612407640L, 18410804440L, 17815805240L, 18012205040L, 19813208840L, 17417004440L, 20613208840L, 21411806240L, 19813607040L, 20212806640L, 20212810040L, 22816609840L, 23410806440L, 21412604640L, 21616809440L, 22012804440L, 18816807040L, 20813404240L, 21216005840L, 21411004640L, 23017006840L, 20211408640L, 17210804840L, 22411006440L, 23013605440L, 17212606240L, 17615006640L, 19811810240L, 17012808240L, 21012208640L, 21413607640L, 19416809240L, 21416809640L, 20610606840L, 18810610440L, 22612804240L, 21611004640L, 20411006640L, 20213204840L, 23215410240L, 17011606440L, 22013804640L, 23416408640L, 21414210240L, 20613806640L, 23215005640L, 17016409840L, 23213209640L, 17013607440L, 17012406440L, 21211409040L, 18615006240L, 21211805440L, 21816404240L, 19410804240L, 22212805440L, 21212805440L, 17813409240L, 17416006040L, 17216405240L, 19813405840L, 22614404240L, 21416005440L, 17416608440L, 20015606440L, 19012808440L, 21214808640L, 17013806640L, 17214009240L, 21412807440L, 17812405640L, 20613208840L, 17411005440L, 22812207240L, 21212409240L, 20416405640L, 20011809640L, 18414809040L, 20813206440L, 19613606440L, 17215009440L, 20817004840L, 18412004240L, 21210605240L, 21413208840L, 19415205440L, 20013010440L, 21412805240L, 20015610440L, 19212804240L, 21412804640L, 20012805240L, 21417006040L, 21412808440L, 19212806640L, 19217005840L, 23416204440L, 20816404240L, 21212806640L, 21416008640L, 17012809840L, 18416804640L, 21412806040L, 20216404240L, 23012405640L, 19811004640L, 21816806640L, 17012808440L, 21417008640L, 17012804240L, 17012808640L, 21417008640L}
    );

    private Track track1;
    private PeaksCalculator calculator;

    @Before
    public void init(){
        calculator = new PeaksCalculator(new ClassPathAdapter());
        track1 = ImmutableTrack.builder().id(1).cluster(0).path("/test1.mp3").build();
    }

    @Test
    public void testTrack1(){
        List<TrackHash> hashes = getHashes(track1.getId());
        Optional<Fingerprint> f = calculator.calculate(track1);
        assertTrue(f.isPresent());
        List<TrackHash> result = f.get().getHashes();
        assertNotNull(result);
        assertTrue(result.size() == hashes.size());
        //System.out.println(Arrays.toString(result.stream().mapToLong(TrackHash::getHash).toArray()));
        IntStream.range(0, result.size()).forEach(i -> assertEquals(result.get(i), hashes.get(i)));
    }

    private List<TrackHash> getHashes(long trackId){
        AtomicInteger time = new AtomicInteger();
        return Arrays.stream(hashes.get(trackId))
                .mapToObj(hash -> ImmutableTrackHash.builder().trackId(trackId).time(time.getAndIncrement()).hash(hash).build())
                .collect(Collectors.toList());
    }
}
