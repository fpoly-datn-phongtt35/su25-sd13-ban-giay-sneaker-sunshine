<template>
    <main class="blog-container">
        <header>
            <h1>👟 Sneaker – Không chỉ là giày, đó là phong cách sống</h1>
        </header>

        <!-- Ảnh hero (đã thu nhỏ 90%) -->
        <figure class="hero">
            <img :src="images.hero" alt="Ảnh cận cảnh đôi sneaker phối streetwear trong bối cảnh ngoài trời"
                loading="lazy" decoding="async" />
            <figcaption>Đôi sneaker – nhân vật chính của mọi outfit</figcaption>
        </figure>

        <article class="content" aria-label="Bài viết giới thiệu về sneaker">
            <section>
                <h2>🔥 Vì sao sneaker lại được yêu thích đến vậy?</h2>
                <p>
                    Sneaker không đơn giản chỉ là một đôi giày để đi lại, mà nó còn là
                    <b>biểu tượng của thời trang đường phố</b>. Từ những đôi Jordan
                    huyền thoại, Yeezy cá tính cho đến Air Force 1 cổ điển, sneaker luôn
                    khiến người mang trở nên <b>ngầu, trẻ trung và tự tin</b>.
                </p>
            </section>

            <section>
                <h2>🎨 Sneaker – nơi thời trang gặp gỡ nghệ thuật</h2>
                <p>
                    Giới sneakerhead coi mỗi đôi giày như một <b>tác phẩm nghệ thuật</b>.
                    Từng đường may, phối màu, chất liệu đều thể hiện cá tính riêng. Một
                    đôi sneaker “đỉnh” có thể biến outfit bình thường trở nên cực chất.
                </p>
            </section>

            <!-- Ảnh giữa: Element Plus -->
            <figure class="wide">
                <el-image class="img-wide" :src="images.street" fit="contain" :lazy="true"
                    alt="Sneaker trong phối đồ street style trên đường phố" preview-teleported />
                <figcaption>Street style: nơi sneaker tỏa sáng mạnh mẽ</figcaption>
            </figure>

            <section>
                <h2>🌍 Sneaker và văn hóa toàn cầu</h2>
                <p>
                    Ngày nay, sneaker không còn gói gọn trong thể thao mà đã trở thành
                    <b>xu hướng toàn cầu</b>. Từ rapper, vận động viên đến fashionista, ai
                    cũng sở hữu ít nhất một đôi sneaker yêu thích.
                </p>
            </section>

            <section class="cta" aria-label="Kết luận">
                <h2>👑 Kết</h2>
                <p>
                    Sneaker chính là <b>“vũ khí thời trang”</b> cho bất kỳ ai muốn thể
                    hiện cá tính. Nếu bạn chưa có cho mình một đôi giày chất lượng, hãy
                    bắt đầu ngay hôm nay – bởi <b>mỗi bước chân với sneaker</b> đều mang
                    lại năng lượng mới!
                </p>
            </section>

            <!-- Ảnh cuối: Element Plus -->
            <figure class="wide">
                <el-image class="img-wide" :src="images.studio" fit="contain" :lazy="true"
                    alt="Ảnh studio chụp sneaker, thấy rõ form và chất liệu" preview-teleported />
                <figcaption>Studio shot: thấy rõ form, chất liệu, chi tiết</figcaption>
            </figure>
        </article>
    </main>
</template>

<script setup>
defineOptions({ name: 'Introduce' })

const images = {
    hero:
        'https://file.hstatic.net/1000284478/file/giay-sneaker-la-gi-9_c39be31ae9e3482f932e7389d79015a4.png',
    street:
        'https://file.hstatic.net/1000284478/file/giay-sneaker-la-gi-4_58c40196e51446308ced06a56192c730.png',
    studio:
        'https://file.hstatic.net/1000284478/file/giay-sneaker-la-gi-2_773f3f1256de427dbab76c752c5a7728.png',
}
</script>

<style scoped>
/* Trang rộng tối đa 1500px, căn giữa */
.blog-container {
    max-width: 1500px;
    width: 100%;
    margin: 24px auto;
    padding: 28px;
    background: #fff;
    border-radius: 14px;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

/* Tăng cỡ chữ */
h1 {
    margin: 0 0 16px;
    text-align: center;
    font-size: 2.25rem;
    /* tăng */
    line-height: 1.3;
    color: #111;
}

.content h2 {
    margin: 26px 0 12px;
    font-size: 1.4rem;
    /* tăng */
    color: #333;
}

p {
    margin: 10px 0 0;
    color: #444;
    font-size: 1.125rem;
    /* tăng */
    line-height: 1.9;
    /* dễ đọc hơn */
}

figure {
    margin: 18px 0;
}

figcaption {
    font-size: 1rem;
    /* tăng nhẹ */
    color: #666;
    margin-top: 8px;
    text-align: center;
}

/* Ảnh hero: thu nhỏ 90% */
.hero {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.hero img {
    width: 90%;
    border-radius: 12px;
    aspect-ratio: 16 / 9;
    object-fit: cover;
    transition: transform 0.3s ease;
}

.hero img:hover {
    transform: scale(1.01);
}

/* Khối ảnh wide: đồng bộ 1500px, ảnh 90% container, không crop */
.wide {
    max-width: 1500px;
    width: 100%;
    margin: 18px auto;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.img-wide {
    width: 90%;
}

/* thu nhỏ 90% */
.img-wide :deep(.el-image__inner) {
    width: 100%;
    height: auto;
    object-fit: contain;
    border-radius: 12px;
    transition: transform 0.3s ease;
}

.img-wide:hover :deep(.el-image__inner) {
    transform: scale(1.01);
}

.cta {
    background: #f7f8fa;
    padding: 16px 18px;
    border-left: 4px solid #111;
    border-radius: 10px;
    margin-top: 18px;
}

/* Mobile */
@media (max-width: 640px) {
    .blog-container {
        max-width: 100%;
        margin: 12px;
        padding: 16px;
    }

    h1 {
        font-size: 1.8rem;
    }

    .content h2 {
        font-size: 1.15rem;
    }

    p {
        font-size: 1rem;
        line-height: 1.8;
    }

    .hero img,
    .img-wide {
        width: 100%;
    }

    /* mobile cho full khung */
}
</style>
