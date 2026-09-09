package com.example.data.local

object DatabasePrepopulator {
    suspend fun seedDatabase(db: AppDatabase) {
        val userDao = db.userDao()
        val subjectDao = db.subjectDao()
        val topicDao = db.topicDao()
        val flashcardDao = db.flashcardDao()
        val noteDao = db.noteDao()
        val mistakeDao = db.mistakeDao()
        val studyPlanDao = db.studyPlanDao()
        val examDao = db.examDao()
        val chatDao = db.chatDao()
        val materialDao = db.materialDao()
        val memoryDao = db.aiMemoryDao()

        // 1. User profile
        userDao.insertOrUpdate(
            UserProfileEntity(
                id = 1,
                name = "Alex Rivera",
                educationLevel = "Undergraduate (Year 2)",
                school = "University of California, Berkeley",
                targetGrade = "First-Class Honours (A*)",
                explanationStyle = "Conceptual & Socratic",
                academicLevel = "Intermediate",
                studyGoals = "Master STEM core concepts, eliminate careless exam mistakes, and achieve 90%+ in Finals",
                preferredStudyTimes = "6:00 PM – 10:00 PM",
                streakDays = 7,
                totalStudyMinutes = 540,
                xpPoints = 2340,
                level = 5,
                isOnboarded = true
            )
        )

        // 2. Subjects
        val mathId = subjectDao.insertSubject(
            SubjectEntity(
                name = "Mathematics",
                code = "MATH-201",
                iconName = "calculate",
                colorHex = 0xFF4F46E5, // Indigo
                masteryPercentage = 68,
                description = "Calculus, Linear Algebra & Differential Equations"
            )
        )
        val bioId = subjectDao.insertSubject(
            SubjectEntity(
                name = "Biology",
                code = "BIO-105",
                iconName = "biotech",
                colorHex = 0xFF059669, // Emerald
                masteryPercentage = 74,
                description = "Cellular Biology, Genetics, Evolution & Metabolism"
            )
        )
        val physicsId = subjectDao.insertSubject(
            SubjectEntity(
                name = "Physics",
                code = "PHYS-140",
                iconName = "bolt",
                colorHex = 0xFFD97706, // Amber
                masteryPercentage = 62,
                description = "Classical Mechanics, Electromagnetism & Thermodynamics"
            )
        )
        val chemId = subjectDao.insertSubject(
            SubjectEntity(
                name = "Chemistry",
                code = "CHEM-210",
                iconName = "science",
                colorHex = 0xFFDC2626, // Rose/Red
                masteryPercentage = 58,
                description = "Organic Chemistry, Thermodynamics & Reaction Kinetics"
            )
        )
        val csId = subjectDao.insertSubject(
            SubjectEntity(
                name = "Computer Science",
                code = "CS-160",
                iconName = "code",
                colorHex = 0xFF0284C7, // Sky
                masteryPercentage = 84,
                description = "Data Structures, Algorithms, Graph Theory & Big-O"
            )
        )

        // 3. Topics
        val calculusId = topicDao.insertTopic(
            TopicEntity(
                subjectId = mathId,
                name = "Integration by Parts & Substitution",
                masteryScore = 72,
                confidence = "High",
                accuracy = 78,
                attempts = 24,
                mistakeFrequency = 2,
                prerequisites = "Algebra, Differential Calculus",
                isCriticalGap = false
            )
        )
        val matrixId = topicDao.insertTopic(
            TopicEntity(
                subjectId = mathId,
                name = "Eigenvalues and Eigenvectors",
                masteryScore = 48,
                confidence = "Low",
                accuracy = 52,
                attempts = 16,
                mistakeFrequency = 7,
                prerequisites = "Matrix Determinants, Linear Transformations",
                isCriticalGap = true
            )
        )

        val cellBioId = topicDao.insertTopic(
            TopicEntity(
                subjectId = bioId,
                name = "Photosynthesis & Light Reactions",
                masteryScore = 88,
                confidence = "High",
                accuracy = 92,
                attempts = 30,
                mistakeFrequency = 1,
                prerequisites = "Chloroplast Anatomy",
                isCriticalGap = false
            )
        )
        val respirationId = topicDao.insertTopic(
            TopicEntity(
                subjectId = bioId,
                name = "Cellular Respiration & Krebs Cycle",
                masteryScore = 54,
                confidence = "Low",
                accuracy = 58,
                attempts = 22,
                mistakeFrequency = 8,
                prerequisites = "Glycolysis, ATP Synthase",
                isCriticalGap = true
            )
        )
        topicDao.insertTopic(
            TopicEntity(
                subjectId = bioId,
                name = "Mendelian Genetics & Punnett Squares",
                masteryScore = 82,
                confidence = "High",
                accuracy = 86,
                attempts = 18,
                mistakeFrequency = 2,
                prerequisites = "Meiosis, Alleles",
                isCriticalGap = false
            )
        )

        topicDao.insertTopic(
            TopicEntity(
                subjectId = physicsId,
                name = "Rotational Dynamics & Torque",
                masteryScore = 55,
                confidence = "Medium",
                accuracy = 60,
                attempts = 15,
                mistakeFrequency = 6,
                prerequisites = "Newtonian Mechanics, Kinematics",
                isCriticalGap = true
            )
        )
        topicDao.insertTopic(
            TopicEntity(
                subjectId = physicsId,
                name = "Electromagnetic Induction (Faraday & Lenz's Law)",
                masteryScore = 68,
                confidence = "Medium",
                accuracy = 72,
                attempts = 19,
                mistakeFrequency = 4,
                prerequisites = "Magnetic Fields, Electric Flux",
                isCriticalGap = false
            )
        )

        // 4. Flashcards (Spaced repetition setup)
        val now = System.currentTimeMillis()
        flashcardDao.insertAll(
            listOf(
                FlashcardEntity(
                    subjectId = bioId,
                    topicId = respirationId,
                    front = "What is the net yield of ATP, NADH, and FADH2 per molecule of glucose in the Krebs Cycle alone?",
                    back = "Per glucose (2 cycles of pyruvate):\n- 2 ATP (via GTP)\n- 6 NADH\n- 2 FADH2\n(Also releases 4 CO2).",
                    cardType = "QA",
                    status = "LEARNING",
                    intervalDays = 1,
                    repetitions = 2,
                    easeFactor = 2.3f,
                    dueDate = now - 3600000L,
                    hint = "Remember each glucose yields TWO acetyl-CoA molecules!"
                ),
                FlashcardEntity(
                    subjectId = bioId,
                    topicId = cellBioId,
                    front = "Where do the light-dependent reactions of photosynthesis take place?",
                    back = "In the thylakoid membranes of chloroplasts.",
                    cardType = "QA",
                    status = "REVIEW",
                    intervalDays = 4,
                    repetitions = 4,
                    easeFactor = 2.6f,
                    dueDate = now + 86400000L
                ),
                FlashcardEntity(
                    subjectId = mathId,
                    topicId = calculusId,
                    front = "State the formula for Integration by Parts.",
                    back = "∫ u · dv = u·v - ∫ v · du\n\nMnemonic: LIATE (Logarithmic, Inverse trig, Algebraic, Trig, Exponential) to choose 'u'.",
                    cardType = "FORMULA",
                    status = "MASTERED",
                    intervalDays = 14,
                    repetitions = 6,
                    easeFactor = 2.8f,
                    dueDate = now + 600000000L
                ),
                FlashcardEntity(
                    subjectId = mathId,
                    topicId = matrixId,
                    front = "For a square matrix A and eigenvalue λ, what equation defines the characteristic polynomial?",
                    back = "det(A - λ·I) = 0\n\nWhere I is the identity matrix of the same dimension.",
                    cardType = "FORMULA",
                    status = "LEARNING",
                    intervalDays = 1,
                    repetitions = 1,
                    easeFactor = 2.1f,
                    dueDate = now - 1800000L,
                    hint = "Set the determinant to zero."
                ),
                FlashcardEntity(
                    subjectId = physicsId,
                    topicId = 0L,
                    front = "State Faraday's Law of Induction and explain the significance of the minus sign.",
                    back = "ε = -dΦB / dt\n\nThe minus sign represents Lenz's Law: the induced electromotive force creates a current whose magnetic field OPPOSES the change in magnetic flux.",
                    cardType = "QA",
                    status = "LEARNING",
                    intervalDays = 2,
                    repetitions = 2,
                    easeFactor = 2.4f,
                    dueDate = now - 7200000L
                )
            )
        )

        // 5. Intelligent Notes (Cornell format & Outline)
        noteDao.insertAll(
            listOf(
                NoteEntity(
                    subjectId = bioId,
                    title = "Cellular Respiration: Glycolysis vs Krebs Cycle",
                    cues = "• Key Locations\n• Glycolysis Inputs\n• Citric Acid Cycle\n• Electron Transport Chain\n• Common Exam Traps",
                    content = """
# Cellular Respiration Master Summary

## 1. Glycolysis
- **Location:** Cytoplasm (Anaerobic - does not require O2)
- **Input:** 1 Glucose (6C) + 2 ATP + 2 NAD+
- **Net Output:** 2 Pyruvate (3C) + 2 ATP (net) + 2 NADH

## 2. Pyruvate Oxidation (Link Reaction)
- **Location:** Mitochondrial matrix
- 2 Pyruvate → 2 Acetyl-CoA + 2 NADH + 2 CO2

## 3. The Krebs Cycle (Citric Acid Cycle)
- Acetyl-CoA combines with Oxaloacetate (4C) to form Citrate (6C)
- Per glucose molecule (two turns of cycle):
  - 6 NADH generated
  - 2 FADH2 generated
  - 2 ATP produced via substrate-level phosphorylation
  - 4 CO2 released

## 4. Oxidative Phosphorylation
- Located in the inner mitochondrial membrane (cristae).
- NADH & FADH2 donate electrons down the ETC.
- Proton gradient drives ATP Synthase (~28-32 ATP total).
                    """.trimIndent(),
                    summary = "Cellular respiration converts biochemical energy from nutrients into ATP. Crucial exam distinction: Glycolysis occurs in the cytosol without oxygen, whereas the Krebs Cycle and ETC occur inside mitochondria and require oxygen.",
                    tags = "Bio, Respiration, Krebs, ATP",
                    noteType = "CORNELL"
                ),
                NoteEntity(
                    subjectId = mathId,
                    title = "Eigenvalues and Diagonalization Essentials",
                    cues = "• Definition\n• Characteristic Eq\n• Eigenvectors\n• Algebraic Multiplicity\n• Geometric Multiplicity",
                    content = """
# Eigenvalues & Diagonalization

## The Fundamental Relation
For matrix **A**, a non-zero vector **v** is an eigenvector with eigenvalue **λ** if:
`A·v = λ·v`

## Step-by-Step Procedure:
1. Compute the characteristic equation: `det(A - λI) = 0`
2. Solve the polynomial for eigenvalues `λ1, λ2, ...`
3. For each eigenvalue, find the null space: `(A - λI)v = 0`
4. Row reduce `[A - λI | 0]` to find basis vectors.
5. If the sum of dimensions of eigenspaces equals n, A is diagonalizable:
   `A = P · D · P⁻¹`
                    """.trimIndent(),
                    summary = "Eigenvalues scale eigenvectors without changing their direction. A matrix is diagonalizable iff every eigenvalue has equal algebraic and geometric multiplicities.",
                    tags = "Math, LinearAlgebra, Matrices",
                    noteType = "CORNELL"
                )
            )
        )

        // 6. Mistake Book (Categorized error log with active reasoning)
        mistakeDao.insertAll(
            listOf(
                MistakeEntity(
                    subjectId = bioId,
                    topic = "Cellular Respiration",
                    question = "How many net ATP molecules are produced during the Krebs cycle per glucose molecule?",
                    studentAnswer = "1 ATP",
                    correctAnswer = "2 ATP",
                    explanation = "A single turn of the cycle produces 1 ATP (via GTP). However, one glucose molecule yields TWO acetyl-CoA molecules, driving TWO cycles, thus netting 2 ATP.",
                    errorType = "Careless mistake (Scale factor omitted)",
                    difficulty = "Medium",
                    isMastered = false
                ),
                MistakeEntity(
                    subjectId = mathId,
                    topic = "Eigenvalues",
                    question = "Find the eigenvalues of matrix A = [[3, 1], [0, 2]].",
                    studentAnswer = "λ = 5, -1",
                    correctAnswer = "λ = 3, 2",
                    explanation = "Matrix A is triangular! The eigenvalues of any upper or lower triangular matrix are simply the entries along its main diagonal: 3 and 2.",
                    errorType = "Conceptual misunderstanding (Property overlooked)",
                    difficulty = "Easy",
                    isMastered = false
                ),
                MistakeEntity(
                    subjectId = physicsId,
                    topic = "Rotational Dynamics",
                    question = "A solid cylinder and a thin spherical shell roll down an incline without slipping from rest. Which reaches the bottom first?",
                    studentAnswer = "The spherical shell because it is lighter",
                    correctAnswer = "The solid cylinder",
                    explanation = "Mass does not matter! The solid cylinder has moment of inertia I = 1/2 M R^2 (c = 0.5), while the spherical shell has I = 2/3 M R^2 (c = 0.67). Smaller rotational inertia fraction means higher linear acceleration.",
                    errorType = "Reasoning error (Intuition override)",
                    difficulty = "Hard",
                    isMastered = false
                )
            )
        )

        // 7. Dynamic Study Plans
        studyPlanDao.insertAll(
            listOf(
                StudyPlanEntity(
                    subjectId = bioId,
                    dayOfWeek = "Today",
                    startTime = "18:00",
                    endTime = "18:35",
                    topic = "Cellular Respiration & Krebs Cycle Gap Drill",
                    technique = "Feynman Technique & Retrieval",
                    isCompleted = false
                ),
                StudyPlanEntity(
                    subjectId = mathId,
                    dayOfWeek = "Today",
                    startTime = "18:45",
                    endTime = "19:25",
                    topic = "Eigenvalues: Diagonalization & Practice Exam Problems",
                    technique = "Worked Example Analysis",
                    isCompleted = false
                ),
                StudyPlanEntity(
                    subjectId = bioId,
                    dayOfWeek = "Today",
                    startTime = "19:35",
                    endTime = "19:50",
                    topic = "Active Spaced Flashcard Review (12 Cards Due)",
                    technique = "Spaced Retrieval (Anki protocol)",
                    isCompleted = false
                ),
                StudyPlanEntity(
                    subjectId = physicsId,
                    dayOfWeek = "Tomorrow",
                    startTime = "17:30",
                    endTime = "18:15",
                    topic = "Faraday's & Lenz's Law: Induced EMF in Moving Rods",
                    technique = "Problem Solving Drill",
                    isCompleted = false
                )
            )
        )

        // 8. Upcoming Exams
        examDao.insertAll(
            listOf(
                ExamEntity(
                    subjectId = bioId,
                    examName = "BIO-105 Midterm Examination",
                    examBoard = "University Dept Curriculum",
                    examDateTimestamp = now + (11 * 86400000L),
                    targetScore = "92%",
                    highFrequencyTopics = "Cellular Respiration, Light Reactions, DNA Replication, Transcription",
                    predictedWeakAreas = "Krebs Cycle ATP bookkeeping, Calvin cycle Rubisco regulation",
                    daysLeft = 11
                ),
                ExamEntity(
                    subjectId = mathId,
                    examName = "MATH-201 Linear Algebra Final",
                    examBoard = "Departmental Final",
                    examDateTimestamp = now + (23 * 86400000L),
                    targetScore = "95%",
                    highFrequencyTopics = "Eigenvalues & Eigenvectors, Subspaces, Gram-Schmidt Orthogonalization",
                    predictedWeakAreas = "Diagonalizability criteria, defective matrices",
                    daysLeft = 23
                )
            )
        )

        // 9. Uploaded Material Analyses
        materialDao.insertMaterial(
            MaterialEntity(
                subjectId = bioId,
                title = "Lecture 7: Cellular Bioenergetics & Respiration Slides.pdf",
                fileType = "PDF",
                summary = "In-depth breakdown of metabolic pathways, aerobic vs anaerobic respiration, and mitochondrial chemiosmosis.",
                keyConcepts = "Glycolysis, Citric Acid Cycle, Oxidative Phosphorylation, Proton Motive Force, ATP Yield",
                formulas = "C6H12O6 + 6 O2 → 6 CO2 + 6 H2O + ~30-32 ATP; ΔG° = -686 kcal/mol",
                potentialExamQuestions = "1. Trace carbon atoms from pyruvate through two turns of the Krebs Cycle.\n2. Contrast substrate-level vs oxidative phosphorylation.\n3. Explain how DNP (uncoupler) disrupts ATP synthesis.",
                difficultSections = "Complex IV cytochrome oxidase proton pumping mechanism; difference between Malate-Aspartate and Glycerol-Phosphate shuttles."
            )
        )

        // 10. AI Memories
        memoryDao.insertMemory(
            AiMemoryEntity(
                category = "Weakness",
                content = "Alex repeatedly confuses the ATP net yield per single turn of Krebs cycle vs per molecule of glucose."
            )
        )
        memoryDao.insertMemory(
            AiMemoryEntity(
                category = "Goal",
                content = "Targeting 90%+ in upcoming BIO-105 Midterm in 11 days. Prefers step-by-step Socratic breakdowns."
            )
        )
        memoryDao.insertMemory(
            AiMemoryEntity(
                category = "Learning Style",
                content = "Thrives with visual analogies and intuitive Feynman-style summaries before formula derivations."
            )
        )

        // 11. Sample AI Tutor initial chat greeting
        chatDao.insertMessage(
            ChatMessageEntity(
                sessionId = "default",
                role = "assistant",
                content = """
Hello Alex! I'm your **Microschool AI Tutor**.


I've reviewed your current progress across your subjects:
• **Biology**: Your Photosynthesis mastery is high (88%), but **Cellular Respiration** has a critical knowledge gap (54%).
• **Mathematics**: Integration is steady (72%), but **Eigenvalues** needs revision before your exam in 23 days.

Would you like to do a **10-minute Socratic drill** on the Krebs cycle, test yourself with flashcards, or deep-dive into any concept?
                """.trimIndent(),
                teachingMode = "Socratic"
            )
        )
    }
}
