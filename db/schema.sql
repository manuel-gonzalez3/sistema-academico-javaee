-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 12-06-2026 a las 09:01:10
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `inscripciones`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `alumno_carrera`
--

CREATE TABLE `alumno_carrera` (
  `IdCarrera` int(11) NOT NULL,
  `estado` enum('INSCRIPTO','BAJA','APROBADO') NOT NULL DEFAULT 'INSCRIPTO',
  `fechaInscripcion` timestamp NOT NULL DEFAULT current_timestamp(),
  `dni` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `carrera`
--

CREATE TABLE `carrera` (
  `IdCarrera` int(11) NOT NULL,
  `Nombre` varchar(50) NOT NULL,
  `Activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `comision`
--

CREATE TABLE `comision` (
  `IdComision` int(11) NOT NULL,
  `IdCurso` int(11) NOT NULL,
  `CantAlumnos` int(11) NOT NULL,
  `Activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `comision_alumno`
--

CREATE TABLE `comision_alumno` (
  `IdComision` int(11) NOT NULL,
  `estado` enum('cursando','regular','aprobado','libre','promocionado') DEFAULT 'cursando',
  `nota` int(11) DEFAULT NULL,
  `dni` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `curso`
--

CREATE TABLE `curso` (
  `IdCurso` int(11) NOT NULL,
  `Nombre` varchar(50) NOT NULL,
  `IdCarrera` int(11) NOT NULL,
  `dniDocente` int(11) DEFAULT NULL,
  `Activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `examen`
--

CREATE TABLE `examen` (
  `idExamen` int(11) NOT NULL,
  `dniDocente` int(11) NOT NULL,
  `idComision` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `hora` time NOT NULL,
  `estado` enum('pendiente','rendido','cerrado','cancelado') NOT NULL DEFAULT 'pendiente',
  `activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `examen_alumno`
--

CREATE TABLE `examen_alumno` (
  `idExamen` int(11) NOT NULL,
  `dniAlumno` int(11) NOT NULL,
  `asistencia` enum('pendiente','presente','ausente') NOT NULL DEFAULT 'pendiente',
  `nota` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `persona`
--

CREATE TABLE `persona` (
  `dni` int(11) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `apellido` varchar(50) NOT NULL,
  `direccion` varchar(100) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `fechaNacimiento` date DEFAULT NULL,
  `sexo` char(1) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `persona`
--

INSERT INTO `persona` (`dni`, `nombre`, `apellido`, `direccion`, `email`, `fechaNacimiento`, `sexo`, `telefono`) VALUES
(0, 'DemoAdmin', 'Usuario', NULL, 'admin@demo', NULL, NULL, NULL),
(1, 'demo', 'docente', NULL, 'docente@demo', NULL, NULL, NULL),
(2, 'Demo', 'Alumno', '', 'alumno@demo', NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario`
--

CREATE TABLE `usuario` (
  `dni` int(11) NOT NULL,
  `password` varchar(100) DEFAULT NULL,
  `tipoUsuario` enum('admin','docente','alumno') NOT NULL DEFAULT 'alumno',
  `activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuario`
--

INSERT INTO `usuario` (`dni`, `password`, `tipoUsuario`, `activo`) VALUES
(0, 'admin', 'admin', 1),
(1, 'docente', 'docente', 1),
(2, 'alumno', 'alumno', 1);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `alumno_carrera`
--
ALTER TABLE `alumno_carrera`
  ADD PRIMARY KEY (`dni`,`IdCarrera`),
  ADD KEY `idx_ac_carrera` (`IdCarrera`);

--
-- Indices de la tabla `carrera`
--
ALTER TABLE `carrera`
  ADD PRIMARY KEY (`IdCarrera`);

--
-- Indices de la tabla `comision`
--
ALTER TABLE `comision`
  ADD PRIMARY KEY (`IdComision`),
  ADD KEY `IdCurso` (`IdCurso`);

--
-- Indices de la tabla `comision_alumno`
--
ALTER TABLE `comision_alumno`
  ADD PRIMARY KEY (`IdComision`,`dni`),
  ADD KEY `idx_idcomision` (`IdComision`),
  ADD KEY `fk_ca_persona` (`dni`);

--
-- Indices de la tabla `curso`
--
ALTER TABLE `curso`
  ADD PRIMARY KEY (`IdCurso`),
  ADD KEY `IdCarrera` (`IdCarrera`),
  ADD KEY `dniDocente` (`dniDocente`);

--
-- Indices de la tabla `examen`
--
ALTER TABLE `examen`
  ADD PRIMARY KEY (`idExamen`),
  ADD KEY `fk_examen_docente` (`dniDocente`),
  ADD KEY `fk_examen_comision` (`idComision`);

--
-- Indices de la tabla `examen_alumno`
--
ALTER TABLE `examen_alumno`
  ADD PRIMARY KEY (`idExamen`,`dniAlumno`),
  ADD KEY `fk_ea_alumno` (`dniAlumno`);

--
-- Indices de la tabla `persona`
--
ALTER TABLE `persona`
  ADD PRIMARY KEY (`dni`);

--
-- Indices de la tabla `usuario`
--
ALTER TABLE `usuario`
  ADD PRIMARY KEY (`dni`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `carrera`
--
ALTER TABLE `carrera`
  MODIFY `IdCarrera` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `comision`
--
ALTER TABLE `comision`
  MODIFY `IdComision` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT de la tabla `curso`
--
ALTER TABLE `curso`
  MODIFY `IdCurso` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT de la tabla `examen`
--
ALTER TABLE `examen`
  MODIFY `idExamen` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `alumno_carrera`
--
ALTER TABLE `alumno_carrera`
  ADD CONSTRAINT `fk_ac_carrera` FOREIGN KEY (`IdCarrera`) REFERENCES `carrera` (`IdCarrera`) ON DELETE CASCADE;

--
-- Filtros para la tabla `comision`
--
ALTER TABLE `comision`
  ADD CONSTRAINT `comision_fk` FOREIGN KEY (`IdCurso`) REFERENCES `curso` (`IdCurso`) ON DELETE CASCADE;

--
-- Filtros para la tabla `comision_alumno`
--
ALTER TABLE `comision_alumno`
  ADD CONSTRAINT `comision_alumno_fk_1` FOREIGN KEY (`IdComision`) REFERENCES `comision` (`IdComision`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_ca_persona` FOREIGN KEY (`dni`) REFERENCES `persona` (`dni`) ON DELETE CASCADE;

--
-- Filtros para la tabla `curso`
--
ALTER TABLE `curso`
  ADD CONSTRAINT `curso_fk_1` FOREIGN KEY (`IdCarrera`) REFERENCES `carrera` (`IdCarrera`) ON DELETE CASCADE;

--
-- Filtros para la tabla `examen`
--
ALTER TABLE `examen`
  ADD CONSTRAINT `fk_examen_comision` FOREIGN KEY (`idComision`) REFERENCES `comision` (`IdComision`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_examen_docente` FOREIGN KEY (`dniDocente`) REFERENCES `persona` (`dni`) ON DELETE CASCADE;

--
-- Filtros para la tabla `examen_alumno`
--
ALTER TABLE `examen_alumno`
  ADD CONSTRAINT `fk_ea_alumno` FOREIGN KEY (`dniAlumno`) REFERENCES `persona` (`dni`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_ea_examen` FOREIGN KEY (`idExamen`) REFERENCES `examen` (`idExamen`) ON DELETE CASCADE;

--
-- Filtros para la tabla `usuario`
--
ALTER TABLE `usuario`
  ADD CONSTRAINT `usuario_fk` FOREIGN KEY (`dni`) REFERENCES `persona` (`dni`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
