import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitRepositoryUrlPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        try {
            File gitDir = resolveMainGitDir(project.getRootDir());
            try (Repository repository = new FileRepositoryBuilder()
                    .setGitDir(gitDir)
                    .readEnvironment()
                    .build()) {
                String originUrl = repository.getConfig().getString("remote", "origin", "url");
                System.out.println("Origin URL: " + originUrl);
                String url = originUrl
                                .replace(".git", "")
                                .replace("https://github.com/", "")
                                .replace("git@github.com:", "");
                System.out.println("repository: " + url);
                project.getExtensions().getExtraProperties().set("gitRepository", url);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File resolveMainGitDir(File workDir) throws IOException {
        File dotGit = new File(workDir, ".git");
        if (!dotGit.exists()) {
            throw new IOException("Cannot find .git in " + workDir);
        }
        if (dotGit.isDirectory()) {
            return dotGit;
        }
        // In a worktree, .git is a file containing "gitdir: <path>"
        String content = Files.readString(dotGit.toPath()).trim();
        if (!content.startsWith("gitdir: ")) {
            throw new IOException("Unexpected .git file content: " + content);
        }
        Path worktreeGitDir = dotGit.toPath().getParent().resolve(content.substring("gitdir: ".length())).normalize();
        // Follow commondir to find the main .git directory
        Path commondirFile = worktreeGitDir.resolve("commondir");
        if (Files.exists(commondirFile)) {
            String commondir = Files.readString(commondirFile).trim();
            return worktreeGitDir.resolve(commondir).normalize().toFile();
        }
        return worktreeGitDir.toFile();
    }
}
